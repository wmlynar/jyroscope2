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

	private final long timeoutNanos;
	private final long delayNanos;
	private final int count;
	private final ThreadPoolExecutor executor;
	private final PriorityBlockingQueue<MessageProcessor<T>> timeoutQueue;
	private Lock lock;
	private final Condition schedulerCondition;
//	private final ConcurrentLinkedDeque<T> queue = new ConcurrentLinkedDeque<>();
	private final CircularBuffer<T> queue;
	private final AtomicLong nextTimeout = new AtomicLong();
	private boolean isProcessing = false;
	private Runnable command;

	private int counter;

	public MessageProcessor(Consumer<T> callback, int queueLength, int timeout, ThreadPoolExecutor executor,
			PriorityBlockingQueue<MessageProcessor<T>> timeoutQueue, Lock lock, Condition schedulerCondition) {
		this.queue = new CircularBuffer<>(queueLength);
		this.timeoutNanos = timeout * 1_000_000l;
		this.delayNanos = 0;
		this.count = 0;
		this.executor = executor;
		this.timeoutQueue = timeoutQueue;
		this.lock = lock;
		this.schedulerCondition = schedulerCondition;
		this.nextTimeout.set(System.nanoTime() + this.timeoutNanos);

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

	public MessageProcessor(Supplier<Boolean> callable, int delay, int interval, int count, ThreadPoolExecutor executor,
			PriorityBlockingQueue<MessageProcessor<T>> timeoutQueue, Lock lock, Condition schedulerCondition) {
		this.queue = new CircularBuffer<>(1);
		this.timeoutNanos = interval * 1_000_000l;
		this.delayNanos = delay * 1_000_000l;
		this.count = count;
		this.executor = executor;
		this.timeoutQueue = timeoutQueue;
		this.lock = lock;
		this.schedulerCondition = schedulerCondition;
		this.nextTimeout.set(System.nanoTime() + delayNanos);
		this.counter = 0;

		this.command = () -> {
			T message;
			while (count == 0 || counter < count) {
				if (timeoutNanos > 0) {
					synchronized (MessageProcessor.this) {
						message = queue.pollFirst();
						if (message == null) {
							isProcessing = false;
							return;
						}
					}
				}
				++counter;
				boolean result = callable.get();
				if (!result) {
					stopTimer();
				}
			}
		};
	}

	public void addMessage(T message) {
		synchronized (this) {
			if (timeoutNanos > 0) {
				nextTimeout.set(System.nanoTime() + timeoutNanos);
				lock.lock();
				try {
					timeoutQueue.remove(this);
					timeoutQueue.offer(this);
					schedulerCondition.signalAll();
				} finally {
					lock.unlock();
				}
			} else if (delayNanos > 0) {
				lock.lock();
				try {
					timeoutQueue.remove(this);
				} finally {
					lock.unlock();
				}
			}
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