package org.ros.rosjava.roslaunch.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ros.rosjava.roslaunch.logging.PrintLog;

import com.github.jy2.log.NodeNameManager;

public class JarUtils {

	public static ThreadGroup runJar(String fileName, String[] params) {
		File file = new File(fileName);

		// add jar to classpatch (from main thread)
		try {
			// dynamically add jars in java 9,10,11
			// https://www.youtube.com/watch?v=Ho9b_0kvN3o

			// URLClassLoader child = new URLClassLoader(new URL[] { file.toURI().toURL() },
			// JarUtils.class.getClassLoader());
			URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			Method m = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
			m.setAccessible(true);
			m.invoke(urlClassLoader, file.toURI().toURL());
		} catch (Throwable e) {
			PrintLog.error(e.getMessage());
			PrintLog.error(ExceptionUtils.getStackTrace(e));
		}

		// run main method (in new thread)
		ThreadGroup tg = new ThreadGroup(NodeNameManager.getNextThreadGroupName());
		Thread t = new Thread(tg, new Runnable() {
			@Override
			public void run() {
				try {
					String main = getMainClass(file);
					if (main == null) {
						throw new RuntimeException(
								"Cannot find attribute Main-Class in manifest inside jar file: " + fileName);
					}
//					Class<?> cls = Class.forName(main, true, child);
					Class<?> cls = Class.forName(main);
					Method meth = cls.getMethod("main", String[].class);
					if (meth == null) {
						throw new RuntimeException("Cannot find main function in class " + main
								+ " inside manifest inside jar file: " + fileName);
					}
					meth.invoke(null, (Object) params);
				} catch (Throwable e) {
					PrintLog.error(e.getMessage());
					PrintLog.error(ExceptionUtils.getStackTrace(e));
				}
			}
		});
		t.start();
		return tg;
	}

	public static String getMainClass(File jar) throws FileNotFoundException, IOException {

		try (FileInputStream in = new FileInputStream(jar); JarInputStream jarStream = new JarInputStream(in)) {
			Manifest mf = jarStream.getManifest();
			if (mf == null) {
				throw new RuntimeException("Cannot find manifest inside jar file: " + jar.getAbsolutePath());
			}
			Attributes attributes = mf.getMainAttributes();
			return attributes.getValue("Main-Class");
		}
	}
}
