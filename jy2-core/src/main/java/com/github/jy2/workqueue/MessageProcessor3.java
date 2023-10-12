package com.github.jy2.workqueue;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MessageProcessor3<T> {

	public static final Object TIMEOUT_MARKER = new Object();

	private final long timeout;
	private final ThreadPoolExecutor executor;
	private final ScheduledExecutorService scheduledExecutor;
	private volatile ScheduledFuture<?> future;
	private final CircularBuffer<T> queue;
	private boolean isProcessing = false;
	private Runnable command;
	private Runnable callTimeout = () -> callTimeout();
	private boolean rescheduleAndProcessTimeout = false;

	public MessageProcessor3(Consumer<T> callback, int queueLength, int timeout, ThreadPoolExecutor executor,
			ScheduledExecutorService scheduledExecutor) {
		this.queue = new CircularBuffer<>(queueLength);
		this.timeout = timeout;
		this.executor = executor;
		this.scheduledExecutor = scheduledExecutor;

		this.command = () -> {
			T message;
			while (true) {
				synchronized (MessageProcessor3.this) {
					message = queue.pollFirst();
					if (message == null) {
						if (rescheduleAndProcessTimeout) {
							rescheduleAndProcessTimeout = false;
							this.future = scheduledExecutor.scheduleWithFixedDelay(callTimeout, timeout, timeout,
									TimeUnit.MILLISECONDS);
							// message = TIMEOUT_MARKER; <- not needed, because message is null anyway
						} else {
							// timeout is already scheduled
							isProcessing = false;
							return;
						}
					}
				}
				if (message == TIMEOUT_MARKER) {
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
		synchronized (this) {
			if (timeout > 0) {
				if (future != null) {
					future.cancel(false);
				}
				rescheduleAndProcessTimeout = false;
				future = scheduledExecutor.scheduleWithFixedDelay(callTimeout, timeout, timeout, TimeUnit.MILLISECONDS);
			}
			queue.addLast(message);
			if (!isProcessing) {
				startProcessingMessages();
			}
		}
	}

	public void callTimeout() {
		synchronized (this) {
			queue.clear();
			queue.setMarker((T) TIMEOUT_MARKER);
			if (isProcessing) {
				if (future != null) {
					future.cancel(false);
				}
				rescheduleAndProcessTimeout = true;
				return;
			} else {
				rescheduleAndProcessTimeout = false;
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
		synchronized (this) {
			queue.clear();
			rescheduleAndProcessTimeout = false;
			if (future != null) {
				future.cancel(false);
			}
		}
	}
}