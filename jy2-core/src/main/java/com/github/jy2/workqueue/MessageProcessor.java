package com.github.jy2.workqueue;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

public class MessageProcessor<T> implements Comparable<MessageProcessor<T>> {

	public static final Object TIMEOUT_MARKER = new Object();

	private final Consumer<T> callback;
	private final int queueLength;
	private final int timeout;
	private final ThreadPoolExecutor executor;
	private final PriorityBlockingQueue<MessageProcessor<T>> timeoutQueue;
	private Lock lock;
	private final Condition schedulerCondition;
//	private final ConcurrentLinkedDeque<T> queue = new ConcurrentLinkedDeque<>();
	private final CircularBuffer<T> queue;
	private final AtomicLong nextTimeout = new AtomicLong();
	private boolean isProcessing = false;

	public MessageProcessor(Consumer<T> callback, int queueLength, int timeout, ThreadPoolExecutor executor,
			PriorityBlockingQueue<MessageProcessor<T>> timeoutQueue, Lock lock, Condition schedulerCondition) {
		this.queue = new CircularBuffer<>(queueLength);
		this.callback = callback;
		this.queueLength = queueLength;
		this.timeout = timeout * 1_000_000;
		this.executor = executor;
		this.timeoutQueue = timeoutQueue;
		this.lock = lock;
		this.schedulerCondition = schedulerCondition;
		this.nextTimeout.set(System.currentTimeMillis() + timeout);
	}

	public void addMessage(T message) {
		synchronized (this) {
			if (message == TIMEOUT_MARKER && queue.size() > 0) {
				return;
			}
// not needed because circular buffer will handle it by definition
//			if (queue.size() >= queueLength) {
//				queue.pollFirst();
//			}
			queue.addLast(message);
			nextTimeout.set(System.nanoTime() + timeout);
			if (!isProcessing) {
				startProcessingMessages();
			}
		}
		lock.lock();
		try {
			timeoutQueue.remove(this);
			timeoutQueue.offer(this);
			schedulerCondition.signalAll();
		} finally {
			lock.unlock();
		}
	}

	private void startProcessingMessages() {
		isProcessing = true;
		try {
			executor.execute(() -> {
				T message;
				while (true) {
					synchronized (this) {
						message = queue.pollFirst();
						if (message == null) {
							isProcessing = false;
							return;
						}
					}
					if (message == TIMEOUT_MARKER) {
						callback.accept(null);
					} else {
						callback.accept(message);
					}
				}
			});
		} catch (RejectedExecutionException e) {
			isProcessing = false;
		}
	}

	public long getNextTimeout() {
		return nextTimeout.get();
	}

	@Override
	public int compareTo(MessageProcessor<T> o) {
		return Long.compare(this.nextTimeout.get(), o.nextTimeout.get());
	}

}