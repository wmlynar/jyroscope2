package com.github.jy2.uberjar;

import java.io.File;
import java.util.HashMap;

public class Util {

	private static final HashMap<String, String> jarCache = new HashMap<String, String>();
	private static final HashMap<String, String> packageCache = new HashMap<String, String>();

	public static String resolveEnv(final String argStr, final String[] arguments) {
		// Make sure there is only one argument given to the arg command
		if (arguments.length == 0) {
			throw new RuntimeException("$(env var) must specify an environment variable [env].");
		} else if (arguments.length != 1) {
			throw new RuntimeException("$(env var) command only accepts one argument [arg" + argStr + "]");
		}

		String varName = arguments[0];

		// Make sure the environment variable is defined
		String value = System.getenv(varName);
		if (value == null) {
			throw new RuntimeException("environment variable '" + varName + "' is not set");
		}

		return value;
	}
	
	public static String resolveOptenv(
			final String argStr,
			final String[] arguments)
	{
		if (arguments.length == 0) {
			throw new RuntimeException("$(optenv var) must specify an environment variable [optenv].");
		}
		else
		{
			// Look up the value of the environment variable
			String value = System.getenv(arguments[0]);
			if (value != null) {
				return value;  // Found the environment variable
			}

			// The environment variable is not defined (but that's fine)
			if (arguments.length == 1) {
				return "";  // No default value provided
			}

			// optenv called with a default value -- return that

			// Skip the first argument (name of the environment variable)
			value = "";
			for (int index = 1; index < arguments.length; ++index)
			{
				// Separate each argument with a space
				if (index > 1) value += " ";  // No space before the first argument
				value += arguments[index];
			}

			return value;
		}
	}

	public static String resolveFind(final String argStr, final String[] arguments) {
		// Make sure there is only one argument given to the find command
		if (arguments.length != 1) {
			throw new RuntimeException("$(find pkg) command only accepts one argument [find " + argStr + "]");
		}

		// Look up the path to the ros package
		return getPackageDir(arguments[0]);
	}

	public static String getPackageDir(final String pkg) {
		// check if package is in cache
		String cached = packageCache.get(pkg);
		if (cached != null) {
			return cached;
		}

		// Check for the ROS package path
		String rosPackagePath = EnvVar.ROS_PACKAGE_PATH.getReqNonEmpty();

		// Check every one of the folders configured in the package path to
		// determine if it contains the package we are looking for
		String[] packageFolders = rosPackagePath.split(":");
		for (String folder : packageFolders) {
			String match = findPackage(folder, pkg);
			if (match.length() > 0) {
				// add to cache
				packageCache.put(pkg, match);
				return match; // Found the package folder!
			}
		}

		throw new RuntimeException("Package not found: " + pkg);
	}

	private static String findPackage(final String directory, final String pkg) {
		File file = new File(directory);
		if (file.exists() && file.isDirectory()) {
			File catkinIgnore = new File(file, "CATKIN_IGNORE");
			if (catkinIgnore.exists() && catkinIgnore.isFile()) {
				return "";
			}

			if (file.getName().compareTo(pkg) == 0) {
				File packageManifest = new File(file, "package.xml");
				if (packageManifest.exists() && packageManifest.isFile()) {
					return file.getAbsolutePath(); // Found the package -- stop looking!
				}
			}

			// Packages cannot be stored inside of one another, thus
			// if we hit a package directory then there is no point to
			// continue searching its sub directories
			File packageManifest = new File(file, "package.xml");
			if (packageManifest.exists() && packageManifest.isFile()) {
				return ""; // Not the package, stop looking
			}

			// This folder is NOT a package folder, thus it could contain
			// packages and all of its subfolders need to be checked
			// to determine if they are the package we are looking for
			String[] subFolders = file.list();
			for (String folderSubItem : subFolders) {
				File folderPath = new File(file, folderSubItem);

				String match = findPackage(folderPath.getAbsolutePath(), pkg);
				if (match.length() > 0) {
					return match; // Found the package folder!
				}
			}
		}

		return ""; // Did not find the package folder
	}
}
