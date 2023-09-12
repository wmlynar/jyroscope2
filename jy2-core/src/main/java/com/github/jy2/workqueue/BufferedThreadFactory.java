package com.github.jy2.workqueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class BufferedThreadFactory implements ThreadFactory {

	private final AtomicInteger threadNumber = new AtomicInteger(1);
	private final Thread backgroundThreadCreator;
	private BlockingQueue<MyThread> threadBuffer;

	public BufferedThreadFactory(int bufferSize) {
		threadBuffer = new ArrayBlockingQueue<>(bufferSize);

		// Pre-fill the buffer
		for (int i = 0; i < bufferSize; i++) {
			threadBuffer.offer(new MyThread());
		}

		// Create a background thread to refill the buffer
		backgroundThreadCreator = new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					threadBuffer.put(new MyThread());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, "buffered-pool-thread-creator");

		backgroundThreadCreator.setDaemon(true); // So it doesn't prevent JVM shutdown
		backgroundThreadCreator.start();
	}

	@Override
	public Thread newThread(Runnable r) {
		MyThread thread = null;
		try {
			thread = threadBuffer.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (thread == null) {
			return new Thread(r);
		}
		thread.setRunnable(r);
		return thread;
	}

	private class MyThread extends Thread {
		private Runnable runnable;

		public MyThread() {
			super("buffered-pool-thread-" + threadNumber.getAndIncrement());
		}

		public void setRunnable(Runnable runnable) {
			this.runnable = runnable;
		}

		@Override
		public void run() {
			if (runnable != null) {
				runnable.run();
			}
		}
	}
}