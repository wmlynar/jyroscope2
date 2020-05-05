package com.github.jy2.di.internal;

import java.lang.Thread.State;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.di.LogSeldom;

public class ExitProcessOnUncaughtException implements UncaughtExceptionHandler {

	public static final LogSeldom LOG = JyroscopeDi.getLog();
	
	public static final int LOG_PUBLISH_TIME = 2000;

	static public void register() {
		Thread.setDefaultUncaughtExceptionHandler(new ExitProcessOnUncaughtException());
	}

	private ExitProcessOnUncaughtException() {
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		// avoid getting into infinite loop if it gets another out of memory error
		Thread.setDefaultUncaughtExceptionHandler(null);
		try {
			LOG.error("Uncaught exception caught in thread " + t, e);
			printFullCoreDump();
			// give logging system time to publish the logs
			try {
				Thread.sleep(LOG_PUBLISH_TIME);
			} catch (InterruptedException e1) {
			}
		} finally {
			Runtime.getRuntime().halt(1);
		}
	}

	public static void printFullCoreDump() {
		LOG.error("All Stack Traces:\n" + getAllStackTraces() + "\n" + "Heap:\n" + getHeapInfo());
	}

	public static String getAllStackTraces() {
		String ret = "";
		Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();

		for (Entry<Thread, StackTraceElement[]> entry : allStackTraces.entrySet()) {
			State state = entry.getKey().getState();
			if (state == State.WAITING || state == State.TIMED_WAITING) {
				continue;
			}
			ret += getThreadInfo(entry.getKey(), entry.getValue()) + "\n";
		}
		return ret;
	}

	public static String getHeapInfo() {
		String ret = "";
		List<MemoryPoolMXBean> memBeans = ManagementFactory.getMemoryPoolMXBeans();
		for (MemoryPoolMXBean mpool : memBeans) {
			MemoryUsage usage = mpool.getUsage();

			String name = mpool.getName();
			long used = usage.getUsed();
			long max = usage.getMax();
			int pctUsed = (int) (used * 100 / max);
			ret += " " + name + " total: " + (max / 1000) + "K, " + pctUsed + "% used\n";
		}
		return ret;
	}

	public static String getThreadInfo(Thread thread, StackTraceElement[] stack) {
		String ret = "";
		ret += "\n\"" + thread.getName() + "\"";
		if (thread.isDaemon())
			ret += " daemon";
		ret += " prio=" + thread.getPriority() + " tid=" + String.format("0x%08x", thread.getId());
		if (stack.length > 0)
			ret += " in " + stack[0].getClassName() + "." + stack[0].getMethodName() + "()";
		ret += "\n   java.lang.Thread.State: " + thread.getState() + "\n";
		ret += getStackTrace(stack);
		return ret;
	}

	public static String getStackTrace(StackTraceElement[] stack) {
		String ret = "";
		for (StackTraceElement element : stack)
			ret += "\tat " + element + "\n";
		return ret;
	}
}