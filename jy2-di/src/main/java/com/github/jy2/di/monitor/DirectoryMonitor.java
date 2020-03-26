package com.github.jy2.di.monitor;

import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;

public class DirectoryMonitor {
	ArrayList<Consumer<String>> consumers = new ArrayList<Consumer<String>>();
	boolean keepRunning = true;
	WatchService watcher;
	Thread thread;
	WatchKey key;

	public void monitor(Path path) throws IOException {
		watcher = FileSystems.getDefault().newWatchService();
		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (keepRunning) {
					WatchKey key;
					try {
						key = watcher.take();
					} catch (InterruptedException x) {
						continue;
					}

					for (WatchEvent<?> event : key.pollEvents()) {
						WatchEvent.Kind<?> kind = event.kind();

						if (kind == OVERFLOW) {
							continue;
						}

						WatchEvent<Path> ev = (WatchEvent<Path>) event;
						Path filename = ev.context();
						updateConsumers(filename.toString());
					}

					boolean valid = key.reset();
					if (!valid) {
						break;
					}

				}
			}
		}, "file watcher");
		thread.start();
		key = path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
	}

	public synchronized void addListener(Consumer<String> consumer) throws IOException {
		consumers.add(consumer);
	}

	public synchronized void stop() {
		consumers.clear();
		keepRunning = false;
		if (key != null) {
			key.cancel();
		}
		thread.interrupt();
	}

	private synchronized void updateConsumers(String filename) {
		for (Consumer<String> c : consumers) {
			c.accept(filename);
		}
	}

}
