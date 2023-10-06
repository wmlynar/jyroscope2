package com.github.jy2.log;

import java.util.HashMap;

public class NodeNameManager {

	private static HashMap<String, Holder<String>> threadMap = new HashMap<>();
	private static int counter = 0;

	public static synchronized void setNodeName(String nodeName) {
		String threadGroupName = Thread.currentThread().getThreadGroup().getName();
		Holder<String> holder = threadMap.get(threadGroupName);
		if (holder == null) {
			holder = new Holder<>(threadGroupName);
			threadMap.put(threadGroupName, holder);
		}
		holder.value = nodeName;
	}

	public static synchronized String getNodeName() {
		String threadGroupName = Thread.currentThread().getThreadGroup().getName();
		Holder<String> holder = threadMap.get(threadGroupName);
		return (holder == null) ? null : holder.value;
	}

	public static synchronized String getNextThreadGroupName() {
		return "hz-" + counter++;
	}

	public static synchronized void removeThreadGroup() {
		threadMap.remove(Thread.currentThread().getThreadGroup().getName());
	}

	public static class Holder<T> {
		public T value;

		public Holder(T value) {
			this.value = value;
		}
	}

}
