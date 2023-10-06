package com.github.jy2.workqueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.jy2.log.NodeNameManager;

public class BufferedThreadFactory implements ThreadFactory {

	private final AtomicInteger threadNumber = new AtomicInteger(1);
	private final Thread backgroundThreadCreator;
	private BlockingQueue<MyThread> threadBuffer;

	public BufferedThreadFactory(int bufferSize) {
		threadBuffer = new ArrayBlockingQueue<>(bufferSize);

		// Pre-fill the buffer
		for (int i = 0; i < bufferSize; i++) {
			ThreadGroup tg = new ThreadGroup(NodeNameManager.getNextThreadGroupName());
			threadBuffer.offer(new MyThread(tg));
		}

		// Create a background thread to refill the buffer
		ThreadGroup tgb = new ThreadGroup(NodeNameManager.getNextThreadGroupName());
		backgroundThreadCreator = new Thread(tgb, () -> {
			NodeNameManager.setNodeName("/fake_node_thread_creator");
			while (true) {
				try {
					ThreadGroup tg = new ThreadGroup(NodeNameManager.getNextThreadGroupName());
					threadBuffer.put(new MyThread(tg));
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
			ThreadGroup tgb = new ThreadGroup(NodeNameManager.getNextThreadGroupName());
			return new MyThread(tgb, r);
		}
		thread.setRunnable(r);
		return thread;
	}

	private class MyThread extends Thread {
		private Runnable runnable;

		public MyThread(ThreadGroup tg) {
			super(tg, "buffered-pool-thread-" + threadNumber.getAndIncrement());
		}

		public MyThread(ThreadGroup tg, Runnable r) {
			super(tg, r, "unbuffered-pool-thread-" + threadNumber.getAndIncrement());
			this.runnable = r;
		}

		public void setRunnable(Runnable runnable) {
			this.runnable = runnable;
		}

		@Override
		public void run() {
			try {
				NodeNameManager.setNodeName("/temporary_fake_node");
				runnable.run();
			} finally {
				NodeNameManager.removeThreadGroup();
			}
		}
	}
}