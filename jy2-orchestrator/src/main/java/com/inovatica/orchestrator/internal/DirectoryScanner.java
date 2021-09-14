package com.inovatica.orchestrator.internal;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class DirectoryScanner {

	public ArrayList<String> scanDirectoryWithoutExtension(String directory, String endsWith) {

		File dir = new File(directory);

		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(endsWith);
			}
		});

		if (files == null) {
			return new ArrayList<>();
		}

		ArrayList<String> arrayList = new ArrayList<>();
		for (File file : files) {
			arrayList.add(nameWithoutExtension(file.getName()));
		}

		return arrayList;
	}

	public String nameWithoutExtension(String name) {
		int pos = name.lastIndexOf(".");
		if (pos < 0) {
			return name;
		}
		return name.substring(0, pos);
	}

}
