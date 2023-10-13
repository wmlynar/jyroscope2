package com.github.jy2.workqueue;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MessageProcessor<T> {

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

	public MessageProcessor(Consumer<T> callback, int queueLength, int timeout, ThreadPoolExecutor executor,
			ScheduledExecutorService scheduledExecutor) {
		this.queue = new CircularBuffer<>(queueLength);
		this.timeout = timeout;
		this.executor = executor;
		this.scheduledExecutor = scheduledExecutor;

		this.command = () -> {
			T message;
			while (keepRunning) {
				synchronized (MessageProcessor.this) {
					message = queue.pollFirst();
					if (message == null) {
						isProcessing = false;
						return;
					}
				}
				if (message == RESCHEDULE_AND_TIMEOUT_MARKER) {
					synchronized (MessageProcessor.this) {
						if (future != null) {
							future.cancel(false);
						}
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
				if (future != null) {
					future.cancel(false);
				}
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

	@SuppressWarnings("unchecked")
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

	public void stop() {
		keepRunning = false;
		synchronized (this) {
			queue.clear();
			if (future != null) {
				future.cancel(false);
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
}