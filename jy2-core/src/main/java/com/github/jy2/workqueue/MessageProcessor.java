package com.github.jy2.workqueue;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MessageProcessor<T> implements Comparable<MessageProcessor<T>> {

	public static final Object TIMEOUT_MARKER = new Object();

	private final int timeout;
	private final ThreadPoolExecutor executor;
	private final PriorityBlockingQueue<MessageProcessor<T>> timeoutQueue;
	private Lock lock;
	private final Condition schedulerCondition;
//	private final ConcurrentLinkedDeque<T> queue = new ConcurrentLinkedDeque<>();
	private final CircularBuffer<T> queue;
	private final AtomicLong nextTimeout = new AtomicLong();
	private boolean isProcessing = false;
	private Runnable command;

	public MessageProcessor(Consumer<T> callback, int queueLength, int timeout, ThreadPoolExecutor executor,
			PriorityBlockingQueue<MessageProcessor<T>> timeoutQueue, Lock lock, Condition schedulerCondition) {
		this.queue = new CircularBuffer<>(queueLength);
		this.timeout = timeout * 1_000_000;
		this.executor = executor;
		this.timeoutQueue = timeoutQueue;
		this.lock = lock;
		this.schedulerCondition = schedulerCondition;
		this.nextTimeout.set(System.nanoTime() + this.timeout);

		this.command = () -> {
			T message;
			while (true) {
				synchronized (MessageProcessor.this) {
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
		};
	}

	public MessageProcessor(Supplier<Boolean> callable, int delay, int interval, ThreadPoolExecutor executor,
			PriorityBlockingQueue<MessageProcessor<T>> timeoutQueue, Lock lock, Condition schedulerCondition) {
		this.queue = new CircularBuffer<>(1);
		this.timeout = interval * 1_000_000;
		this.executor = executor;
		this.timeoutQueue = timeoutQueue;
		this.lock = lock;
		this.schedulerCondition = schedulerCondition;
		this.nextTimeout.set(System.nanoTime() + delay * 1_000_000);

		this.command = () -> {
			T message;
			while (true) {
				synchronized (MessageProcessor.this) {
					message = queue.pollFirst();
					if (message == null) {
						isProcessing = false;
						return;
					}
				}
				boolean result = callable.get();
				if (!result) {
					stopTimer();
				}
			}
		};
	}

	public void addMessage(T message) {
		synchronized (this) {
			nextTimeout.set(System.nanoTime() + timeout);
			if (message == TIMEOUT_MARKER) {
				queue.clear();
				queue.setMarker(message);
			} else {
				queue.addLast(message);
			}
			if (!isProcessing) {
				startProcessingMessages();
			}
		}
		if (timeout > 0) {
			lock.lock();
			try {
				timeoutQueue.remove(this);
				timeoutQueue.offer(this);
				schedulerCondition.signalAll();
			} finally {
				lock.unlock();
			}
		}
	}

	public void stop() {
		synchronized (this) {
			queue.clear();
			lock.lock();
			try {
				timeoutQueue.remove(this);
			} finally {
				lock.unlock();
			}
		}
	}

	private void startProcessingMessages() {
		isProcessing = true;
		try {
			executor.execute(command);
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

	public void stopTimer() {
		synchronized (MessageProcessor.this) {
			lock.lock();
			try {
				timeoutQueue.remove(this);
			} finally {
				lock.unlock();
			}
			queue.clear();
			isProcessing = false;
			return;
		}
	}

	public void wakeup() {
		((MessageProcessor) this).addMessage(MessageProcessor.TIMEOUT_MARKER);
	}

}