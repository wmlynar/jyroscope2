package org.ros.rosjava.roslaunch.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ros.rosjava.roslaunch.logging.PrintLog;

import com.github.jy2.classpath.ClasspathUtils;
import com.github.jy2.log.NodeNameManager;

public class JarUtilsCollected {

	private static ArrayList<JarWithParams> jarList = new ArrayList<>();

	public static synchronized void addJar(String fileName, String[] params) {
		jarList.add(new JarWithParams(fileName, params));
	}

	public static synchronized void createContextClassloader() {
		try {
			// DO NOT CLOSE THE CLASSLOADER HERE
			// classes will be loaded in different threads
			URLClassLoader classloader = createUrlClassloaderWithJars(jarList);
			Thread.currentThread().setContextClassLoader(classloader);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static synchronized void runAllAddedJars() {
		try {
			ClassLoader classloader = Thread.currentThread().getContextClassLoader();
			for (JarWithParams jar : jarList) {
				runJarThroughClassloader(classloader, jar.fileName, jar.params);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private static URLClassLoader createUrlClassloaderWithJars(ArrayList<JarWithParams> jars) {
		ArrayList<URL> list = new ArrayList<>();
		try {
			int size = jars.size();
			for (int i = 0; i < size; i++) {
				URL url = new File(jars.get(i).fileName).toURI().toURL();
				list.add(url);
			}
			String classpathVar = System.getenv("ROS_CLASSPATH");
			if (classpathVar != null) {
				ArrayList<URL> expandedClasspathJars = ClasspathUtils.expandClasspath(classpathVar);
				list.addAll(expandedClasspathJars);
			} else {
				System.out.println("ROS_CLASSPATH environment variable not set");
			}
			URL[] urls = list.toArray(new URL[0]);
			return new URLClassLoader(urls, JarUtilsCollected.class.getClassLoader());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	// run main method (in new thread)
	public static void runJarThroughClassloader(ClassLoader classloader, String jarFileName, String[] params) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// ClassLoader classloader = Thread.currentThread().getContextClassLoader();
					Thread.currentThread().setContextClassLoader(classloader);
					String mainClassName = getMainClassName(new File(jarFileName));
					if (mainClassName == null) {
						throw new RuntimeException(
								"Cannot find attribute Main-Class in manifest inside jar file: " + jarFileName);
					}
					Class<?> cls = classloader.loadClass(mainClassName);
					Method method = cls.getMethod("main", String[].class);
					if (method == null) {
						throw new RuntimeException("Cannot find main function in class " + mainClassName
								+ " inside manifest inside jar file: " + jarFileName);
					}
					method.invoke(null, (Object) params);
				} catch (Throwable e) {
					if (e instanceof InvocationTargetException && e.getCause() != null) {
						e = e.getCause();
					}
					PrintLog.error(e.getMessage());
					PrintLog.error(ExceptionUtils.getStackTrace(e));
				}
			}
		});
		t.start();
	}

	public static String getMainClassName(File jar) throws FileNotFoundException, IOException {

		try (FileInputStream in = new FileInputStream(jar); JarInputStream jarStream = new JarInputStream(in)) {
			Manifest mf = jarStream.getManifest();
			if (mf == null) {
				throw new RuntimeException("Cannot find manifest inside jar file: " + jar.getAbsolutePath());
			}
			Attributes attributes = mf.getMainAttributes();
			return attributes.getValue("Main-Class");
		}
	}

	private static class JarWithParams {
		public String fileName;
		public String[] params;

		public JarWithParams(String fileName, String[] params) {
			this.fileName = fileName;
			this.params = params;
		}
	}
}
