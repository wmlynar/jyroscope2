package com.github.jy2.tf.mat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleDirectedGraph;

import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.di.LogSeldom;
import com.github.jy2.di.annotations.Parameter;
import com.github.jy2.di.ros.TimeProvider;
import com.github.jy2.tf.mat.dataobjects.LatestTime;
import com.github.jy2.tf.mat.dataobjects.Path;
import com.github.jy2.tf.mat.dataobjects.StringPair;
import com.github.jy2.tf.mat.internal.TransformBuffer;

import go.jyroscope.ros.geometry_msgs.Quaternion;
import go.jyroscope.ros.geometry_msgs.TransformStamped;
import go.jyroscope.ros.std_msgs.Header;
import go.jyroscope.ros.tf2_msgs.TFMessage;

public class TfManager {

	public final LogSeldom LOG = JyroscopeDi.getLog();
	public final double TRANSFORM_TIMEOUT = 0.2;

	public final static double DEAFULT_TIMEOUT_S = 0.5d;
	public final static int SMALL_TIMEOUT_MS = 50;

	// for checking if TfManager needs to be reset because of transform back in
	// time (looped logs)
	private final static double SECONDS_BACK_IN_TIME_TO_RESET = 5;

	@Parameter("/static_transform_keyword")
	public String staticTransformKeyword = "static";

	@Parameter("/semi_transform_keyword")
	public String semiTransformKeyword = "semi";

	// structures to access transform buffers (transforms sorted over time)
	private final ArrayList<TransformBuffer> transformBufferList = new ArrayList<>();
	private final HashMap<StringPair, TransformBuffer> transformBufferMap = new HashMap<>();

	// cache of the calculated path along transform buffer indexes
	private final HashMap<StringPair, Path> pathCache = new HashMap<>();

	// graph used to compute path between coordinate frames using the transforms
	private final Graph<String, Integer> graph = new SimpleDirectedGraph<>(Integer.class);

	// mutex used for synchronisation of all public methods
	private final Object mutex = new Object();
	private final Object notify = new Object();

	// objects used for avoiding to allocate memory
	private final StringPair stringPair = new StringPair("", "");
	private final Matrix4d mat = new Matrix4d();
	private final TransformMatrix trans = new TransformMatrix();

	// lates available transform time
	private double lastTime = Double.NEGATIVE_INFINITY;

	// temporary object used to avoid memory allocation
	private final LatestTime latestTime = new LatestTime();

	// temporary transform matrix
	TransformMatrix temp = new TransformMatrix();

	/**
	 * Add transform list.
	 * 
	 * @param transformList Transform message to be added
	 */
	public void add(TFMessage transformList) {
		// synchronized, bacuse adding all transforms need to an atomic operation
		// (for example drif cancelation)
		synchronized (mutex) {
			for (TransformStamped transform : transformList.transforms) {
				add(transform);
			}
		}
		synchronized (notify) {
			notify.notifyAll();
		}
	}

	/**
	 * Add single transform.
	 * 
	 * @param transform Transform to be added
	 */
	public void add(TransformStamped transform) {
		synchronized (mutex) {
			if (!isQuaternionCorrect(transform.transform.rotation)) {
				LOG.infoSeldom("Skipping transform with wrong quaternion " + transform.transform.rotation.toString());
				return;
			}

			checkIfShouldReset(transform.header.toSeconds());

			Matrix4d matrix = new Matrix4d();
			transform.get(matrix);

			TransformBuffer tb = getTransformBuffer(transform);
			tb.addTransform(transform.header.toSeconds(), matrix);
		}
		synchronized (notify) {
			notify.notifyAll();
		}
	}

	/**
	 * Obtain transform at specific time.
	 * 
	 * @param from Parent transform
	 * @param to   Child transform
	 * @param time Time of the transform
	 * @param mat  Output matrix where transform will be stored
	 * @return <code>true</code> when transform was found
	 */
	public boolean getTransform(String from, String to, double time, Matrix4d mat) {
		if (from.equals(to)) {
			mat.setIdentity();
			return true;
		}
		synchronized (mutex) {
			Path path = getPath(from, to);
			if (path == null || path.indexes.length == 0) {
				return false;
			}
			return multiplyMatricesInPath(path.indexes, path.inverted, time, mat);
		}
	}

	/**
	 * Obtain transform at specific time and latest semi-static transforms. WARNING!
	 * use with care, it can cause strange effects when used incorrectly. Especially
	 * NEVER publish computations based on this again to TfManager, only publish for
	 * example as odometry or pose.
	 * 
	 * @param from Parent transform
	 * @param to   Child transform
	 * @param time Time of the transform
	 * @param mat  Output matrix where transform will be stored
	 * @return <code>true</code> when transform was found
	 */
	public boolean getTransformSemi(String from, String to, double time, Matrix4d mat) {
		if (from.equals(to)) {
			mat.setIdentity();
			return true;
		}
		synchronized (mutex) {
			Path path = getPath(from, to);
			if (path == null || path.indexes.length == 0) {
				return false;
			}
			return multiplyMatricesInPathSemi(path.indexes, path.inverted, time, mat);
		}
	}

	/**
	 * Obtain latest available transform chain.
	 * 
	 * @param from Parent transform
	 * @param to   Child transform
	 * @param mat  Output matrix where transform will be stored
	 * @return <code>true</code> when transform was found
	 */
	public boolean getTransformLatest(String from, String to, Matrix4d mat) {
		if (from.equals(to)) {
			mat.setIdentity();
			return true;
		}
		synchronized (mutex) {
			Path path = getPath(from, to);
			if (path == null || path.indexes.length == 0) {
				return false;
			}
			if (!getLatestTime(path.indexes, latestTime)) {
				return false;
			}
			return multiplyMatricesInPath(path.indexes, path.inverted, latestTime.time, mat);
		}
	}

	/**
	 * Obtain latest available transform chain and latest semi-static transforms.
	 * WARNING! use with care, it can cause strange effects when used incorrectly.
	 * Especially NEVER publish computations based on this again to TfManager, only
	 * publish for example as odometry or pose.
	 * 
	 * @param from Parent transform
	 * @param to   Child transform
	 * @param mat  Output matrix where transform will be stored
	 * @return <code>true</code> when transform was found
	 */
	public boolean getTransformSemiLatest(String from, String to, Matrix4d mat) {
		if (from.equals(to)) {
			mat.setIdentity();
			return true;
		}
		synchronized (mutex) {
			Path path = getPath(from, to);
			if (path == null || path.indexes.length == 0) {
				return false;
			}
			if (!getSemiLatestTime(path.indexes, latestTime)) {
				return false;
			}
			return multiplyMatricesInPathSemi(path.indexes, path.inverted, latestTime.time, mat);
		}
	}

	void sleep(int ms) {
		synchronized (notify) {
			try {
				notify.wait(ms);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * Waits for transform to be available by sleeping short amount of time and
	 * checking for availability.
	 * 
	 * @param timeProvider Time provider
	 * @param from         Parent transform
	 * @param to           Child transform
	 * @param time         Time of the transform
	 * @param mat          Output matrix where transform will be stored
	 * @return <code>true</code> when transform was found, <code>true</code> when
	 *         transform does not exist
	 */
	public boolean waitForTransform(TimeProvider timeProvider, String from, String to, double time, Matrix4d mat) {
		if (from.equals(to)) {
			mat.setIdentity();
			return true;
		}
		Path path;
		synchronized (mutex) {
			path = getPath(from, to);
			if (path == null || path.indexes.length == 0) {
				return false;
			}
		}
		double start = timeProvider.now();
		double now = start;
		while (true) {
			synchronized (mutex) {
				if (transformExists(path.indexes, time)) {
					break;
				}
			}
			now = timeProvider.now();
			if (now - start > DEAFULT_TIMEOUT_S) {
				break;
			}
			sleep(SMALL_TIMEOUT_MS);
		}
		// System.out.println(String.format("waited %.3f\n", now-start));
		return multiplyMatricesInPath(path.indexes, path.inverted, time, mat);
	}

	/**
	 * Waits for semi-static transform to be available by sleeping short amount of
	 * time and checking for availability.
	 * 
	 * @param timeProvider Time provider
	 * @param from         Parent transform
	 * @param to           Child transform
	 * @param time         Time of the transform
	 * @param mat          Output matrix where transform will be stored
	 * @return <code>true</code> when transform was found, <code>true</code> when
	 *         transform does not exist.
	 */
	public boolean waitForTransformSemi(TimeProvider timeProvider, String from, String to, double time, Matrix4d mat) {
		if (from.equals(to)) {
			mat.setIdentity();
			return true;
		}
		Path path;
		synchronized (mutex) {
			path = getPath(from, to);
			if (path == null || path.indexes.length == 0) {
				return false;
			}
		}
		double start = timeProvider.now();
		double now = start;
		while (true) {
			synchronized (mutex) {
				if (transformExistsSemi(path.indexes, time)) {
					break;
				}
			}
			now = timeProvider.now();
			if (now - start > DEAFULT_TIMEOUT_S) {
				break;
			}
			sleep(SMALL_TIMEOUT_MS);
		}
		// System.out.println(String.format("waited %.3f\n", now-start));
		return multiplyMatricesInPathSemi(path.indexes, path.inverted, time, mat);
	}

	/**
	 * Get the transform composed from individually latest transforms. WARNING! use
	 * with care, it can cause strange effects when used incorrectly. Especially
	 * NEVER publish computations based on this again to TfManager, only publish for
	 * example as odometry or pose.
	 * 
	 * @param from      Parent transform
	 * @param to        Child transform
	 * @param transform Output where transform will be stored
	 * @return <code>true</code> when transform was found
	 */
	public boolean getTransformLatestIndividually(String from, String to, TransformStamped transform) {
		if (from.equalsIgnoreCase(to)) {
			transform.setIdentity();
			transform.childFrameId = from;
			transform.header.frameId = from;
			// todo: what value for time?
			// transform.time=?
			return true;
		}
		synchronized (mutex) {
			Path path = getPath(from, to);
			if (path == null || path.indexes.length == 0) {
				return false;
			}
			if (multiplyMatricesInPathIndividuallyLatest(path.indexes, path.inverted, temp)) {
				transform.header.setSeconds(temp.time);
				transform.header.frameId = temp.parentFrameId;
				transform.childFrameId = temp.childFrameId;
				transform.transform.set(temp.matrix);
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Get the transform composed from individually latest transforms. WARNING! use
	 * with care, it can cause strange effects when used incorrectly. Especially
	 * NEVER publish computations based on this again to TfManager, only publish for
	 * example as odometry or pose.
	 * 
	 * @param from   Parent transform
	 * @param to     Child transform
	 * @param matrix Output matrix where transform will be stored
	 * @return <code>true</code> when transform was found
	 */
	public boolean getTransformLatestIndividually(String from, String to, Matrix4d matrix) {
		if (from.equalsIgnoreCase(to)) {
			matrix.setIdentity();
			return true;
		}
		synchronized (mutex) {
			Path path = getPath(from, to);
			if (path == null || path.indexes.length == 0) {
				LOG.warnSeldom("Cannot create path for transform: " + from + "->" + to);
				return false;
			}
			boolean result = multiplyMatricesInPathIndividuallyLatest(path.indexes, path.inverted, temp);
			if (result) {
				matrix.set(temp.matrix);
			}
			return result;
		}
	}

	/**
	 * Get the time of the latest available transform chain.
	 * 
	 * @param from Parent transform
	 * @param to   Child transform
	 * @return Time of the transform.
	 */
	public double getLatestTime(String from, String to) {
		synchronized (mutex) {
			Path path = getPath(from, to);
			if (path == null) {
				return Double.NEGATIVE_INFINITY;
			}
			if (getLatestTime(path.indexes, latestTime)) {
				return latestTime.time;
			} else {
				return Double.NEGATIVE_INFINITY;
			}
		}
	}

	/**
	 * Get the time of the latest available transform chain.
	 * 
	 * @param from Parent transform
	 * @param to   Child transform
	 * @return Time of the transform.
	 */
	public double getSemiLatestTime(String from, String to) {
		synchronized (mutex) {
			Path path = getPath(from, to);
			if (path == null) {
				return Double.NEGATIVE_INFINITY;
			}
			if (getSemiLatestTime(path.indexes, latestTime)) {
				return latestTime.time;
			} else {
				return Double.NEGATIVE_INFINITY;
			}
		}
	}

	/**
	 * Returns list of known transforms.
	 * 
	 * @return List of transforms
	 */
	public ArrayList<TransformStamped> getTransformList() {
		TransformMatrix tm = new TransformMatrix();
		ArrayList<TransformStamped> list = new ArrayList<>();
		for (TransformBuffer tb : transformBufferList) {
			tb.getTransformLatest(tm);
			TransformStamped t = new TransformStamped();
			t.header = new Header();
			t.header.setSeconds(tm.time);
			t.header.frameId = tm.parentFrameId;
			t.childFrameId = tm.childFrameId;
			t.set(tm.matrix);
			list.add(t);
		}
		return list;
	}

	private void checkIfShouldReset(double time) {
		if (time + SECONDS_BACK_IN_TIME_TO_RESET < lastTime) {
			for (TransformBuffer tb : transformBufferList) {
				tb.reset(time);
			}

		}
		lastTime = time;
	}

	private TransformBuffer getTransformBuffer(TransformStamped transform) {
		boolean isStaticTransform = transform.childFrameId.contains(staticTransformKeyword);
//				|| transform.type == Transform.STATIC;
		stringPair.set(transform.header.frameId, transform.childFrameId);
		TransformBuffer tb = transformBufferMap.get(stringPair);
		if (tb == null) {
			tb = createTransformBuffer(transform.header.frameId, transform.childFrameId, isStaticTransform);
		} else if (tb.isStaticTransform != isStaticTransform) {
			// throw new IllegalArgumentException("Transform cannot change status of
			// isStaticTransform");
			// static transform keyword changed - clear transform buffer
			tb = createTransformBuffer(transform.header.frameId, transform.childFrameId, isStaticTransform);
		}
//		tb.isSemiStatic = transform.type == Transform.SEMI;
		tb.isSemiStatic = transform.childFrameId.contains(semiTransformKeyword);
		return tb;
	}

	private Path getPath(String from, String to) {
		stringPair.set(from, to);
		Path path = pathCache.get(stringPair);
		if (path == null) {
			// path not in cache
			path = computePathAndAddToCache(from, to);
		}
		return path;
	}

	private TransformBuffer createTransformBuffer(String from, String to, boolean isStaticTransform) {
		int transformBufferIndexInList = transformBufferList.size();

		TransformBuffer tb = new TransformBuffer(from, to, isStaticTransform);
		StringPair sp = new StringPair(from, to);
		transformBufferMap.put(sp, tb);
		transformBufferList.add(tb);

		// add graph nodes
		if (!graph.containsVertex(from)) {
			graph.addVertex(from);
		}
		if (!graph.containsVertex(to)) {
			graph.addVertex(to);
		}
		// add graph edge
		if (!graph.containsEdge(from, to)) {
			graph.addEdge(from, to, transformBufferIndexInList);
		}
		return tb;
	}

	private boolean transformExists(int[] path, double time) {
		for (int i = 0; i < path.length; i++) {
			if (!transformBufferList.get(path[i]).getTransform(time, mat)) {
				return false;
			}
		}
		return true;
	}

	private boolean transformExistsSemi(int[] path, double time) {
		for (int i = 0; i < path.length; i++) {
			if (transformBufferList.get(path[i]).isSemiStatic) {
				if (!transformBufferList.get(path[i]).getTransformLatest(mat)) {
					return false;
				}
			} else {
				if (!transformBufferList.get(path[i]).getTransform(time, mat)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean multiplyMatricesInPath(int[] path, boolean inverted, double time, Matrix4d matrix) {
		if (!transformBufferList.get(path[0]).getTransform(time, matrix)) {
			LOG.warnSeldom("Missing transform in transfrom buffer at specific time " + String.format("%.4f", time)
					+ ": " + transformBufferList.get(path[0]).from + "->" + transformBufferList.get(path[0]).to);
			return false;
		}
		for (int i = 1; i < path.length; i++) {
			if (!transformBufferList.get(path[i]).getTransform(time, mat)) {
				LOG.warnSeldom("Missing transform in transfrom buffer at specific time " + String.format("%.4f", time)
						+ ": " + transformBufferList.get(path[i]).from + "->" + transformBufferList.get(path[i]).to);
				return false;
			}
			matrix.mul(mat);
		}
		if (inverted) {
			matrix.invert();
		}
		return true;
	}

	private boolean multiplyMatricesInPathSemi(int[] path, boolean inverted, double time, Matrix4d matrix) {
		if (transformBufferList.get(path[0]).isSemiStatic) {
			if (!transformBufferList.get(path[0]).getTransformLatest(matrix)) {
				LOG.warnSeldom("Missing latest transform in transfrom buffer: " + transformBufferList.get(path[0]).from
						+ "->" + transformBufferList.get(path[0]).to);
				return false;
			}
		} else {
			if (!transformBufferList.get(path[0]).getTransform(time, matrix)) {
				LOG.warnSeldom("Missing transform in transfrom buffer at specific time " + String.format("%.4f", time)
						+ ": " + transformBufferList.get(path[0]).from + "->" + transformBufferList.get(path[0]).to);
				return false;
			}

		}
		for (int i = 1; i < path.length; i++) {
			if (transformBufferList.get(path[i]).isSemiStatic) {
				if (!transformBufferList.get(path[i]).getTransformLatest(mat)) {
					LOG.warnSeldom("Missing latest transform in transfrom buffer: "
							+ transformBufferList.get(path[i]).from + "->" + transformBufferList.get(path[i]).to);
					return false;
				}
			} else {
				if (!transformBufferList.get(path[i]).getTransform(time, mat)) {
					LOG.warnSeldom("Missing transform in transfrom buffer at specific time "
							+ String.format("%.4f", time) + ": " + transformBufferList.get(path[i]).from + "->"
							+ transformBufferList.get(path[i]).to);
					return false;
				}

			}
			matrix.mul(mat);
		}
		if (inverted) {
			matrix.invert();
		}
		return true;
	}

	private boolean multiplyMatricesInPathIndividuallyLatest(int[] path, boolean inverted, TransformMatrix transform) {
		if (!transformBufferList.get(path[0]).getTransformLatest(transform)) {
			LOG.warnSeldom("Missing latest transform in transfrom buffer: " + transformBufferList.get(path[0]).from
					+ "->" + transformBufferList.get(path[0]).to);
			return false;
		}
		for (int i = 1; i < path.length; i++) {
			if (!transformBufferList.get(path[i]).getTransformLatest(trans)) {
				LOG.warnSeldom("Missing latest transform in transfrom buffer: " + transformBufferList.get(path[i]).from
						+ "->" + transformBufferList.get(path[i]).to);
				return false;
			}
			transform.matrix.mul(trans.matrix);
			if (trans.time > transform.time) {
				trans.time = transform.time;
			}
		}
		if (inverted) {
			transform.matrix.invert();
		}
		return true;
	}

	private boolean getLatestTime(int[] path, LatestTime latestTime) {
		latestTime.time = Double.POSITIVE_INFINITY;
		for (int i = 0; i < path.length; i++) {
			if (!transformBufferList.get(path[i]).getLatestTime(latestTime)) {
				return false;
			}
		}
		return true;
	}

	private boolean getSemiLatestTime(int[] path, LatestTime latestTime) {
		latestTime.time = Double.POSITIVE_INFINITY;
		for (int i = 0; i < path.length; i++) {
			if (transformBufferList.get(path[i]).isSemiStatic) {
				// only check if exists
				if (!transformBufferList.get(path[i]).getTransformLatest(mat)) {
					return false;
				}
			} else {
				if (!transformBufferList.get(path[i]).getLatestTime(latestTime)) {
					return false;
				}
			}

		}
		return true;
	}

	private Path computePathAndAddToCache(String from, String to) {
		try {
			Path path;
			GraphPath<String, Integer> graphPath = DijkstraShortestPath.findPathBetween(graph, from, to);
			if (graphPath == null) {
				graphPath = DijkstraShortestPath.findPathBetween(graph, to, from);
				if (graphPath == null) {
					return null;
				}
				path = new Path();
				path.inverted = true;
			} else {
				path = new Path();
			}
			path.indexes = graphPathToArray(graphPath);
			pathCache.put(stringPair, path);
			return path;
		} catch (IllegalArgumentException e) {
			// Dijkstra search did not find path
			return null;
		}
	}

	private int[] graphPathToArray(GraphPath<String, Integer> path) {
		return toIntArray(path.getEdgeList());
	}

	private int[] toIntArray(List<Integer> list) {
		int[] ret = new int[list.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = list.get(i);
		}
		return ret;
	}

	private boolean isQuaternionCorrect(Quat4d q) {
		double sum = q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w;
		return sum > 0.5;
	}

	private boolean isQuaternionCorrect(Quaternion q) {
		double sum = q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w;
		return sum > 0.5;
	}

}
