package com.github.jy2.workqueue;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.jyroscope.types.LinkManager;

public class MessageProcessor2<T> {

	public static final Object TIMEOUT_MARKER = new Object();

	private final long timeout;
	private final long delayNanos;
	private final int count;
	private final ThreadPoolExecutor executor;
	private final ScheduledExecutorService scheduledExecutor;
	private volatile ScheduledFuture<?> future;
//	private final ConcurrentLinkedDeque<T> queue = new ConcurrentLinkedDeque<>();
	private final CircularBuffer<T> queue;
	private boolean isProcessing = false;
	private Runnable command;

	private int counter;

	public MessageProcessor2(Consumer<T> callback, int queueLength, int timeout, ThreadPoolExecutor executor,
			ScheduledExecutorService scheduledExecutor) {
		this.queue = new CircularBuffer<>(queueLength);
		this.timeout = timeout;
		this.delayNanos = 0;
		this.count = 0;
		this.executor = executor;
		this.scheduledExecutor = scheduledExecutor;

		this.command = () -> {
			T message;
			while (true) {
				synchronized (MessageProcessor2.this) {
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

		if (timeout > 0) {
			synchronized (this) {
				this.future = scheduledExecutor.schedule(() -> wakeup(), timeout, TimeUnit.MILLISECONDS);
			}
		}
	}

	public MessageProcessor2(Supplier<Boolean> callable, int delay, int interval, int count,
			ThreadPoolExecutor executor, ScheduledExecutorService scheduledExecutor) {
		this.queue = new CircularBuffer<>(1);
		this.timeout = interval;
		this.delayNanos = delay * 1_000_000l;
		this.count = count;
		this.executor = executor;
		this.scheduledExecutor = scheduledExecutor;
		this.counter = 0;

		this.command = () -> {
			T message;
			while (count == 0 || counter < count) {
				if (timeout > 0) {
					synchronized (MessageProcessor2.this) {
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
					synchronized (MessageProcessor2.this) {
						if (future != null) {
							future.cancel(false);
						}
						isProcessing = false;
						return;
					}
				}
			}
		};
		if (delay > 0) {
			synchronized (this) {
				this.future = scheduledExecutor.schedule(() -> wakeup(), delay, TimeUnit.MILLISECONDS);
			}
		} else {
			wakeup();
		}
	}

	public void addMessage(T message) {
		if (timeout > 0) {
			synchronized (this) {
				if (future != null) {
					future.cancel(false);
				}
				future = LinkManager.scheduledExecutor.scheduleAtFixedRate(() -> wakeup(), timeout, timeout,
						TimeUnit.MILLISECONDS);
			}
		}
		synchronized (this) {
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

	private void startProcessingMessages() {
		isProcessing = true;
		try {
			executor.execute(command);
		} catch (RejectedExecutionException e) {
			isProcessing = false;
		}
	}

	public void wakeup() {
		((MessageProcessor2) this).addMessage(MessageProcessor2.TIMEOUT_MARKER);
	}

	public void stopTimer() {
		synchronized (this) {
			if (future != null) {
				future.cancel(false);
			}
		}
	}

	public void stop() {
		synchronized (this) {
			if (future != null) {
				future.cancel(false);
			}
			queue.clear();
		}
	}
}