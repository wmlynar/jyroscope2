/**
 * Originally written by Gil Tene of Azul Systems, and released to the public
 * domain, as explained at http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.github.jy2.di.internal;

import java.util.concurrent.TimeUnit;
import java.util.function.LongConsumer;

public class JvmHiccupMeterThread extends Thread {

	private static boolean allocateObjects = true;
	public volatile Long lastSleepTimeObj; // public volatile to make sure allocs are not optimized away

	private LongConsumer callback;
	private int resolutionMs;

	private volatile boolean doRun = true;

	public JvmHiccupMeterThread(LongConsumer callback) {
		this(callback, 10);
	}

	public JvmHiccupMeterThread(LongConsumer callback, int resolutionMs) {
		super("jvm-hiccup-metter-thread");
		this.callback = callback;
		this.resolutionMs = resolutionMs;
		setDaemon(true);
	}

	public void run() {
		final long resolutionNsec = (long) (resolutionMs * 1000L * 1000L);
		try {
			long shortestObservedDeltaTimeNsec = Long.MAX_VALUE;
			long timeBeforeMeasurement = Long.MAX_VALUE;
			while (doRun) {
				TimeUnit.NANOSECONDS.sleep(resolutionNsec);
				if (allocateObjects) {
					// Allocate an object to make sure potential allocation stalls are measured.
					lastSleepTimeObj = new Long(timeBeforeMeasurement);
				}
				final long timeAfterMeasurement = System.nanoTime();
				final long deltaTimeNsec = timeAfterMeasurement - timeBeforeMeasurement;
				timeBeforeMeasurement = timeAfterMeasurement;

				if (deltaTimeNsec < 0) {
					// On the very first iteration (which will not time the loop in it's entirety)
					// the delta will be negative, and we'll skip recording.
					continue;
				}

				if (deltaTimeNsec < shortestObservedDeltaTimeNsec) {
					shortestObservedDeltaTimeNsec = deltaTimeNsec;
				}

				long hiccupTimeNsec = deltaTimeNsec - shortestObservedDeltaTimeNsec;

				callback.accept(hiccupTimeNsec);
			}
		} catch (InterruptedException e) {
			System.err.println("JvmHiccupMeterThread terminating...");
		}
	}

	public void terminate() {
		doRun = false;
	}
}
