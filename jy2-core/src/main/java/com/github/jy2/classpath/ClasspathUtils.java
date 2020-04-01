package com.github.jy2.classpath;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

public class ClasspathUtils {

	public static void setContextClassLoader() {
		// add all jars from ROS_CLASSPATH to classpath
		String classpathVar = System.getenv("ROS_CLASSPATH");
		if (classpathVar != null) {
			ArrayList<URL> expandedClasspathJars = expandClasspath(classpathVar);
			if (expandedClasspathJars.size() > 0) {
				URL[] urls = expandedClasspathJars.toArray(new URL[0]);
				URLClassLoader classloader = new URLClassLoader(urls, ClasspathUtils.class.getClassLoader());
				Thread.currentThread().setContextClassLoader(classloader);
			}
		} else {
			System.out.println("ROS_CLASSPATH environment variable not set");
		}
	}

	private static ArrayList<URL> expandClasspath(String classpathVar) {
		// String separator = System.getProperty("path.separator");
		String[] classpathJars = classpathVar.split(",");
		ArrayList<URL> urls = new ArrayList<>();
		for (String jar : classpathJars) {
			if (jar.endsWith("*")) {
				try {
					File file = new File(jar);
					File[] expandedFiles = file.getParentFile().listFiles(new FilenameFilter() {
						public boolean accept(File aDir, String aName) {
							return aName.endsWith(".jar");
						}
					});
					if (expandedFiles != null) {
						for (File expandedFile : expandedFiles) {
							URL url = expandedFile.toURI().toURL();
							urls.add(url);
						}
					} else {
						System.out.println("WARNING: could not expand classpath item " + file.toString());
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			} else {
				try {
					URL url = new File(jar).toURI().toURL();
					urls.add(url);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
		return urls;
	}

}
