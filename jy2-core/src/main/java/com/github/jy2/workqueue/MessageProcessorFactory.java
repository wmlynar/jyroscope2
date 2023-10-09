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

import com.github.jy2.log.NodeNameManager;

public class MessageProcessorFactory<T> {

	private final PriorityBlockingQueue<MessageProcessor<T>> timeoutQueue = new PriorityBlockingQueue<>();
	private final Lock lock = new ReentrantLock();
	private final Condition schedulerCondition = lock.newCondition();

	private BufferedThreadFactory threadFactory;
	private ThreadPoolExecutor executor;
	private int maxThreads;
	private int bufferSize;

	public MessageProcessorFactory(int maxThreads, int bufferSize) {
		this.maxThreads = maxThreads;
		this.bufferSize = bufferSize;
	}

	public MessageProcessor<T> createProcessor(Consumer<T> callback, int queueLength, int timeout) {
		MessageProcessor<T> messageProcessor = new MessageProcessor<T>(callback, queueLength, timeout, getExecutor(),
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
		MessageProcessor<T> messageProcessor = new MessageProcessor<T>(callback, delay, interval, count, getExecutor(),
				this.timeoutQueue, this.lock, this.schedulerCondition);
		if (delay > 0 || interval > 0) {
			lock.lock();
			try {
				timeoutQueue.add(messageProcessor);
				schedulerCondition.signalAll();
			} finally {
				lock.unlock();
			}
		} else {
			messageProcessor.wakeup();
		}
		return messageProcessor;
	}

	public void shutdownAll() {
		this.executor.shutdownNow();
	}

	private void startScheduler() {
		ThreadGroup tgb = new ThreadGroup(NodeNameManager.getNextThreadGroupName());
		Thread t = new Thread(tgb, () -> {
			NodeNameManager.setNodeName("/fake_node_scheduler");
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
						sq.addMessage(MessageProcessor.TIMEOUT_MARKER);
					} else {
						schedulerCondition.awaitNanos(delay);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					lock.unlock();
				}
			}
		}, "work-pool-timer-thread");
		t.setDaemon(true);
		t.start();
	}

	private synchronized ThreadPoolExecutor getExecutor() {
		if (executor == null) {
// replaced with a version that does not allocate so much new objects
//			executor = new ThreadPoolExecutor(... , new NoAllocSynchronousQueue<>(),
// but it does not work
			executor = new ThreadPoolExecutor(1, maxThreads, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(),
					getBufferedThreadFactory());
			startScheduler();
		}
		return executor;
	}

	public synchronized BufferedThreadFactory getBufferedThreadFactory() {
		if (threadFactory == null) {
			threadFactory = new BufferedThreadFactory(bufferSize);
		}
		return threadFactory;
	}

}
