package com.github.jy2.workqueue;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class RepeaterProcessor {

	private final long interval;
	private final ThreadPoolExecutor executor;
	private volatile ScheduledFuture<?> future;
	private boolean isProcessing = false;
	private Runnable command;
	private Runnable wakeup = () -> wakeup();
	private boolean rescheduleAndProcessTimeout = false;
	private volatile boolean keepRunning = true;

	private int counter;

	public RepeaterProcessor(Supplier<Boolean> callable, int delay, int interval, int count,
			ThreadPoolExecutor executor, ScheduledExecutorService scheduledExecutor) {
		this.interval = interval;
		this.executor = executor;
		this.counter = 0;

		this.command = () -> {
			while (keepRunning && (count == 0 || counter < count)) {
				++counter;
				boolean result = callable.get();
				if (!result) {
					stop();
					return;
				}
				if (interval > 0) {
					synchronized (RepeaterProcessor.this) {
						if (rescheduleAndProcessTimeout) {
							if (future != null) {
								future.cancel(false);
							}
							this.future = scheduledExecutor.scheduleWithFixedDelay(wakeup, interval, interval,
									TimeUnit.MILLISECONDS);
							rescheduleAndProcessTimeout = false;
						} else {
							// will be called again by timeout
							isProcessing = false;
							return;
						}
					}
				}
			}
		};

		if (delay > 0 && interval > 0) {
			synchronized (this) {
				if (future != null) {
					future.cancel(false);
				}
				this.future = scheduledExecutor.scheduleWithFixedDelay(wakeup, delay, interval, TimeUnit.MILLISECONDS);
			}
		} else {
			if (interval > 0) {
				synchronized (this) {
					if (future != null) {
						future.cancel(false);
					}
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
					// stop the timer until current processing is finished and restart the timeout
					if (future != null) {
						future.cancel(false);
						future = null;
					}
					rescheduleAndProcessTimeout = true;
				}
			} else {
				startProcessingMessages();
			}
		}
	}

	public void stop() {
		keepRunning = false;
		synchronized (this) {
			if (future != null) {
				future.cancel(false);
				future = null;
			}
			rescheduleAndProcessTimeout = false;
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