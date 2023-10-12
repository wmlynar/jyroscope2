package com.github.jy2.workqueue;

import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.jyroscope.types.LinkManager;

public class MessageProcessorFactory<T> {

	private final PriorityBlockingQueue<MessageProcessor<T>> timeoutQueue = new PriorityBlockingQueue<>();

	private BufferedThreadFactory threadFactory;
	private ThreadPoolExecutor executor;
	public ScheduledExecutorService scheduledExecutor;

	private int maxThreads;
	private int bufferSize;

	public MessageProcessorFactory(int maxThreads, int bufferSize) {
		this.maxThreads = maxThreads;
		this.bufferSize = bufferSize;
	}

	public MessageProcessor<T> createProcessor(Consumer<T> callback, int queueLength, int timeout) {
		return new MessageProcessor<T>(callback, queueLength, timeout, getExecutor(), getScheduledExecutor());
	}

	public RepeaterProcessor createRepeater(Supplier<Boolean> callback, int delay, int interval, int count) {
		return new RepeaterProcessor(callback, delay, interval, count, getExecutor(), getScheduledExecutor());
	}

	public void shutdownAll() {
		executor.shutdown();
		scheduledExecutor.shutdown();
	}

	public synchronized BufferedThreadFactory getBufferedThreadFactory() {
		if (threadFactory == null) {
			threadFactory = new BufferedThreadFactory(bufferSize);
		}
		return threadFactory;
	}

	public synchronized ThreadPoolExecutor getExecutor() {
		if (executor == null) {
			executor = new ThreadPoolExecutor(1, maxThreads, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(),
					getBufferedThreadFactory());
		}
		return executor;
	}

	public synchronized ScheduledExecutorService getScheduledExecutor() {
		if (scheduledExecutor == null) {
			scheduledExecutor = Executors.newScheduledThreadPool(LinkManager.SCHEDULER_POOL_SIZE);
		}
		return scheduledExecutor;
	}
}
