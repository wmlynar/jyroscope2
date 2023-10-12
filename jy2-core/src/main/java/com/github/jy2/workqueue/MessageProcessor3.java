package com.github.jy2.workqueue;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MessageProcessor3<T> {

	public static final Object TIMEOUT_MARKER = new Object();
	public static final Object RESCHEDULE_AND_TIMEOUT_MARKER = new Object();

	private final long timeout;
	private final ThreadPoolExecutor executor;
	private final ScheduledExecutorService scheduledExecutor;
	private volatile ScheduledFuture<?> future;
	private final CircularBuffer<T> queue;
	private boolean isProcessing = false;
	private Runnable command;
	private Runnable callTimeout = () -> callTimeout();
	private volatile boolean keepRunning = true;

	public MessageProcessor3(Consumer<T> callback, int queueLength, int timeout, ThreadPoolExecutor executor,
			ScheduledExecutorService scheduledExecutor) {
		this.queue = new CircularBuffer<>(queueLength);
		this.timeout = timeout;
		this.executor = executor;
		this.scheduledExecutor = scheduledExecutor;

		this.command = () -> {
			T message;
			while (keepRunning) {
				synchronized (MessageProcessor3.this) {
					message = queue.pollFirst();
					if (message == null) {
						isProcessing = false;
						return;
					}
				}
				if (message == RESCHEDULE_AND_TIMEOUT_MARKER) {
					synchronized (MessageProcessor3.this) {
						this.future = scheduledExecutor.scheduleWithFixedDelay(callTimeout, timeout, timeout,
								TimeUnit.MILLISECONDS);
					}
					callback.accept(null);
				} else if (message == TIMEOUT_MARKER) {
					callback.accept(null);
				} else {
					callback.accept(message);
				}
			}
		};

		if (timeout > 0) {
			synchronized (this) {
				this.future = scheduledExecutor.scheduleWithFixedDelay(callTimeout, timeout, timeout,
						TimeUnit.MILLISECONDS);
			}
		}
	}

	public void addMessage(T message) {
		if (!keepRunning) {
			return;
		}
		synchronized (this) {
			if (timeout > 0) {
				if (future != null) {
					future.cancel(false);
				}
				future = scheduledExecutor.scheduleWithFixedDelay(callTimeout, timeout, timeout, TimeUnit.MILLISECONDS);
			}
			queue.addLast(message);
			if (!isProcessing) {
				startProcessingMessages();
			}
		}
	}

	public void callTimeout() {
		if (!keepRunning) {
			return;
		}
		synchronized (this) {
			queue.clear();
			if (isProcessing) {
				// stop the timer until current processing is finished and restart the timeout
				if (future != null) {
					future.cancel(false);
				}
				queue.setMarker((T) RESCHEDULE_AND_TIMEOUT_MARKER);
			} else {
				queue.setMarker((T) TIMEOUT_MARKER);
				startProcessingMessages();
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

	public void stop() {
		keepRunning = false;
		synchronized (this) {
			queue.clear();
			if (future != null) {
				future.cancel(false);
			}
		}
	}
}