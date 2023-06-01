package com.github.jy2.di.internal;

import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.api.LogSeldom;

public class MemoryObserver extends Thread {

	private static final LogSeldom LOG = JyroscopeDi.getLog();
	private static final int MIN_PERCENTAGE = 30;
	private static final int GC_PERIOD_MILLIS = 60 * 1000;

	private long lastTimeGc;

	public MemoryObserver() {
		super("MemoryObserverThread");
	}

	public void run() {
		lastTimeGc = System.currentTimeMillis();
		while (true) {
			try {
				long freeMemory = Runtime.getRuntime().freeMemory();
				long usedMemory = Runtime.getRuntime().totalMemory();
				long percentage = 100L * freeMemory / (freeMemory + usedMemory);
				if (percentage < MIN_PERCENTAGE) {
					LOG.warn("Free memory percentage: " + percentage + ", less than limit: " + MIN_PERCENTAGE
							+ ", free: " + freeMemory / (1024 * 1024) + "MB, used: " + usedMemory / (1024 * 1024)
							+ "MB");
				}
				long time = System.currentTimeMillis();
				if (time - lastTimeGc > GC_PERIOD_MILLIS) {
					System.gc();
					lastTimeGc = time;
				}
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// do nothing
				}
			} catch (Exception e) {
				LOG.error("Exception caught", e);
			}
		}
	}

}
