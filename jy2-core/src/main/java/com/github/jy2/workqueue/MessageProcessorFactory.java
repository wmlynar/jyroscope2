package com.github.jy2.workqueue;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MessageProcessorFactory<T> {

	private final ThreadPoolExecutor executor;
	private final PriorityBlockingQueue<MessageProcessor<T>> timeoutQueue = new PriorityBlockingQueue<>();
	private final Lock lock = new ReentrantLock();
	private final Condition schedulerCondition = lock.newCondition();

	public MessageProcessorFactory(int maxThreads, int bufferSize) {
		this.executor = new ThreadPoolExecutor(1, maxThreads, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(),
				new BufferedThreadFactory(bufferSize));
		startScheduler();
	}

	public MessageProcessor<T> createProcessor(Consumer<T> callback, int queueLength, int timeout) {
		MessageProcessor<T> messageProcessor = new MessageProcessor<T>(callback, queueLength, timeout, this.executor,
				this.timeoutQueue, this.lock, this.schedulerCondition);
		if (timeout > 0) {
			lock.lock();
			try {
				timeoutQueue.add(messageProcessor);
				schedulerCondition.signalAll();
			} finally {
				lock.unlock();
			}
		}
		return messageProcessor;
	}

	public MessageProcessor<T> createRepeater(Supplier<Boolean> callback, int delay, int interval, int count) {
		MessageProcessor<T> messageProcessor = new MessageProcessor<T>(callback, delay, interval, count, this.executor,
				this.timeoutQueue, this.lock, this.schedulerCondition);
		lock.lock();
		try {
			timeoutQueue.add(messageProcessor);
			schedulerCondition.signalAll();
		} finally {
			lock.unlock();
		}
		return messageProcessor;
	}

	public void shutdownAll() {
		this.executor.shutdownNow();
	}

	private void startScheduler() {
		new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					lock.lock();
					MessageProcessor sq = timeoutQueue.peek();

					if (sq == null) {
						schedulerCondition.await();
						continue;
					}

					long delay = sq.getNextTimeout() - System.nanoTime();
					if (delay <= 0) {
//						timeoutQueue.poll();
						sq.addMessage(MessageProcessor.TIMEOUT_MARKER);
//						timeoutQueue.offer(sq);
//						schedulerCondition.signalAll();
					} else {
						schedulerCondition.awaitNanos(delay);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					lock.unlock();
				}
			}
		}, "work-pool-timer-thread").start();
	}

}
