package go.jyroscope.ros.geometry_msgs;

import javax.vecmath.Matrix4d;

import com.github.jy2.messages.Pose2d;
import com.jyroscope.annotations.Hide;
import com.jyroscope.annotations.Message;
import com.jyroscope.annotations.Name;

import go.jyroscope.ros.std_msgs.Header;

@Message("geometry_msgs/TransformStamped")
public class TransformStamped {

	public Header header;
	@Name("child_frame_id")
	public String childFrameId;
	public Transform transform;

	public TransformStamped() {
	}

	public TransformStamped(double time, String parent, String child) {
		header = new Header();
		header.setSeconds(time);
		header.frameId = parent;
		childFrameId = child;
		transform = new Transform();
	}

	public TransformStamped(double time, String parent, String child, Matrix4d m) {
		header = new Header();
		header.setSeconds(time);
		header.frameId = parent;
		childFrameId = child;
		transform = new Transform(m);
	}

	public TransformStamped(double time, String parent, String child, Pose2d p) {
		header = new Header();
		header.setSeconds(time);
		header.frameId = parent;
		childFrameId = child;
		Matrix4d m = new Matrix4d();
		p.get(m);
		transform = new Transform(m);
	}

	@Hide
	public void get(Matrix4d m) {
		transform.get(m);
	}

	@Hide
	public void set(Matrix4d m) {
		if (transform == null) {
			transform = new Transform();
		}
		transform.set(m);
	}

	@Hide
	public void setIdentity() {
		if (transform == null) {
			transform = new Transform();
		}
		transform.setIdentity();
	}

	@Hide
	public Matrix4d asMatrix() {
		Matrix4d mat = new Matrix4d();
		get(mat);
		return mat;
	}

}
