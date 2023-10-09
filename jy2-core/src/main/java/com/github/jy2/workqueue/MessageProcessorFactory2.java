package com.github.jy2.workqueue;

import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.jyroscope.types.LinkManager;

public class MessageProcessorFactory2<T> {

	private final PriorityBlockingQueue<MessageProcessor2<T>> timeoutQueue = new PriorityBlockingQueue<>();
	private final Lock lock = new ReentrantLock();
	private final Condition schedulerCondition = lock.newCondition();

	private BufferedThreadFactory threadFactory;
	private ThreadPoolExecutor executor;
	private int maxThreads;
	private int bufferSize;
	private ScheduledFuture<?> future;

	public MessageProcessorFactory2(int maxThreads, int bufferSize) {
		this.maxThreads = maxThreads;
		this.bufferSize = bufferSize;

	}

	public MessageProcessor2<T> createProcessor(Consumer<T> callback, int queueLength, int timeout) {
		return new MessageProcessor2<T>(callback, queueLength, timeout, getExecutor(), getScheduledExecutor());
	}

	public MessageProcessor2<T> createRepeater(Supplier<Boolean> callback, int delay, int interval, int count) {
		return new MessageProcessor2<T>(callback, delay, interval, count, getExecutor(), getScheduledExecutor());
	}

	public void shutdownAll() {
		this.executor.shutdownNow();
		LinkManager.scheduledExecutor.shutdownNow();
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
		if (LinkManager.scheduledExecutor == null) {
			LinkManager.scheduledExecutor = Executors.newScheduledThreadPool(LinkManager.SCHEDULER_POOL_SIZE);
		}
		return LinkManager.scheduledExecutor;
	}
}
