package com.github.jy2.log;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NodeNameManager2 {

	private static HashMap<String, Holder<String>> threadMap = new HashMap<>();
	private static final AtomicInteger counter = new AtomicInteger(0);

	public static String getNextThreadGroupName() {
		return "jy-" + counter.incrementAndGet();
	}

	public static synchronized void setNodeName(String nodeName) {
		String threadGroupName = Thread.currentThread().getThreadGroup().getName();
		Holder<String> holder = threadMap.get(threadGroupName);
		if (holder == null) {
			holder = new Holder<>();
			threadMap.put(threadGroupName, holder);
		}
		holder.value = nodeName;
	}

	public static synchronized String getNodeName() {
		String threadGroupName = Thread.currentThread().getThreadGroup().getName();
		Holder<String> holder = threadMap.get(threadGroupName);
		return (holder == null) ? null : holder.value;
	}

	public static synchronized void removeThreadGroup() {
		threadMap.remove(Thread.currentThread().getThreadGroup().getName());
	}

	public static class Holder<T> {
		public T value;
	}

}
