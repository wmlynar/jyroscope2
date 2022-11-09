package com.github.jy2.di.internal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.State;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.di.LogSeldom;

public class ExitProcessOnUncaughtException implements UncaughtExceptionHandler {

	public static final LogSeldom LOG = JyroscopeDi.getLog();

	public static final int LOG_PUBLISH_TIME = 2000;

	public static String logFolder = "/tmp";
	public static String memberName = "unknown";

//	private static com.sun.management.ThreadMXBean mxBean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();	

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
			saveCrashLog(t, e);
			LOG.fatal("Uncaught exception caught in thread " + t, e);
			LOG.fatal("All Stack Traces:\n" + getAllStackTraces() + "\n" + "Heap:\n" + getHeapInfo());
			// give logging system time to publish the logs
			unconditionalSleep(LOG_PUBLISH_TIME);
		} finally {
			Runtime.getRuntime().halt(1);
		}
	}

	private void saveCrashLog(Thread t, Throwable e) {
		String stamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String fileName = logFolder + "/crash-" + memberName.replace('/', '_') + "-" + stamp + ".txt";

		// get stack trace
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);

		String content = "Uncaught exception caught in thread " + t + "\n" + stringWriter + "\nAll Stack Traces:\n"
				+ getAllStackTraces() + "\n" + "Heap:\n" + getHeapInfo();

		try {
			new File(logFolder).mkdirs();
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
			writer.write(content);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void unconditionalSleep(int howLong) {
		long start = System.currentTimeMillis();
		while (true) {
			long time = System.currentTimeMillis();
			long dt = howLong - (time - start);
			if (dt <= 0) {
				break;
			}
			try {
				Thread.sleep(dt);
			} catch (InterruptedException e) {
			}
		}
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
			ret += " " + name + " total: " + (max / 1024) + "K, used: " + (used / 1024) + "K, " + pctUsed + "% used\n";
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
//		ret += "\n   allocated bytes: " + mxBean.getThreadAllocatedBytes(thread.getId());
		ret += "\n   java.lang.Thread.state: " + thread.getState() + "\n";
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