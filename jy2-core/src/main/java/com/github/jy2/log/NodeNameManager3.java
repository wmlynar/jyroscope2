package com.github.jy2.log;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NodeNameManager3 {

	private static HashMap<ThreadGroup, Holder<String>> threadMap = new HashMap<>();
	private static final AtomicInteger counter = new AtomicInteger(0);

	public static String getNextThreadGroupName() {
		return "jy-" + counter.incrementAndGet();
	}

	public static synchronized void setNodeName(String nodeName) {
		ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
		Holder<String> holder = threadMap.get(threadGroup);
		if (holder == null) {
			holder = new Holder<>();
			threadMap.put(threadGroup, holder);
		}
		holder.value = nodeName;
	}

	public static synchronized String getNodeName() {
		ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
		Holder<String> holder = threadMap.get(threadGroup);
		return (holder == null) ? "UNKNOWN" : holder.value;
	}

	public static synchronized void removeThreadGroup() {
		threadMap.remove(Thread.currentThread().getThreadGroup());
	}

	public static class Holder<T> {
		public T value;
	}

}
