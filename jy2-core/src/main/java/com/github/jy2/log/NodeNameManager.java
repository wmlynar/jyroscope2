package com.github.jy2.log;

import java.util.HashMap;

public class NodeNameManager {
	
	private static HashMap<String,String> threadMap = new HashMap<>();
	private static int counter = 0;

	public static synchronized void setNodeName(String nodeName) {
		String threadGroupName = Thread.currentThread().getThreadGroup().getName();
		threadMap.put(threadGroupName, nodeName);
	}

	public static synchronized String getNodeName() {
		String threadGroupName = Thread.currentThread().getThreadGroup().getName();
		return threadMap.get(threadGroupName);
	}
	
	public static synchronized String getNextThreadGroupName() {
		return "hz-" + counter++;
	}

	public static synchronized void removeThreadGroup() {
		threadMap.remove(Thread.currentThread().getThreadGroup().getName());
	}
}
