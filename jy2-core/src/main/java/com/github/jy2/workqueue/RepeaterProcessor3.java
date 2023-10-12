package com.github.jy2.workqueue;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class RepeaterProcessor3 {

	public static final Object TIMEOUT_MARKER = new Object();

	private final long interval;
	private final ThreadPoolExecutor executor;
	private final ScheduledExecutorService scheduledExecutor;
	private volatile ScheduledFuture<?> future;
	private boolean isProcessing = false;
	private Runnable command;
	private Runnable wakeup = () -> wakeup();
	private boolean rescheduleAndProcessTimeout = false;
	private volatile boolean keepRunning = true;

	private int counter;

	public RepeaterProcessor3(Supplier<Boolean> callable, int delay, int interval, int count,
			ThreadPoolExecutor executor, ScheduledExecutorService scheduledExecutor) {
		this.interval = interval;
		this.executor = executor;
		this.scheduledExecutor = scheduledExecutor;
		this.counter = 0;

		this.command = () -> {
			while (keepRunning && (count == 0 || counter < count)) {
				++counter;
				boolean result = callable.get();
				if (!result) {
					synchronized (RepeaterProcessor3.this) {
						if (future != null) {
							future.cancel(false);
						}
						isProcessing = false;
						return;
					}
				}
				if (interval > 0) {
					synchronized (RepeaterProcessor3.this) {
						if (rescheduleAndProcessTimeout) {
							rescheduleAndProcessTimeout = false;
							this.future = scheduledExecutor.scheduleWithFixedDelay(wakeup, interval, interval,
									TimeUnit.MILLISECONDS);
						} else {
							// timeout is already scheduled
							isProcessing = false;
							return;
						}
					}
				}
			}
		};

		if (delay > 0 && interval > 0) {
			synchronized (this) {
				this.future = scheduledExecutor.scheduleWithFixedDelay(wakeup, delay, interval, TimeUnit.MILLISECONDS);
			}
		} else {
			if (interval > 0) {
				synchronized (this) {
					this.future = scheduledExecutor.scheduleWithFixedDelay(wakeup, interval, interval,
							TimeUnit.MILLISECONDS);
				}
			}
			wakeup();
		}
	}

	public void wakeup() {
		if (!keepRunning) {
			return;
		}
		synchronized (this) {
			if (isProcessing) {
				if (interval > 0) {
					rescheduleAndProcessTimeout = true;
					if (future != null) {
						future.cancel(false);
					}
				}
			} else {
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
			if (future != null) {
				future.cancel(false);
			}
			rescheduleAndProcessTimeout = false;
		}
	}
}