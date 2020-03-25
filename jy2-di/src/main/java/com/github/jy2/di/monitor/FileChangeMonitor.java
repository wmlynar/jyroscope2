package com.github.jy2.di.monitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.function.Consumer;

public class FileChangeMonitor {

	HashMap<Path, DirectoryMonitor> dms = new HashMap<>();

	public void addFileListener(String file, Runnable runnable) throws IOException {
		File f = new File(file).getAbsoluteFile();
		String name = f.getName();
		Path path = f.toPath().getParent();

		DirectoryMonitor dm = dms.get(path);
		if (dm == null) {
			dm = new DirectoryMonitor();
			dm.monitor(path);
			dms.put(path, dm);
		}
		dm.addListener(new Consumer<String>() {

			@Override
			public void accept(String str) {
				if (str.equals(name)) {
					runnable.run();
				}
			}
		});
	}

	public void removeAllListeners() {
		for (DirectoryMonitor dm : dms.values()) {
			dm.stop();
		}
		dms.clear();
	}
}
