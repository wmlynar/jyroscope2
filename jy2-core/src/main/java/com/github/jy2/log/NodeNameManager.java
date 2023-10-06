package com.github.jy2.log;

import java.util.HashMap;

public class NodeNameManager {

	private static HashMap<ThreadGroup, String> threadMap = new HashMap<>();
	private static int counter = 0;

	public static synchronized void setNodeName(String nodeName) {
		ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
		threadMap.put(threadGroup, nodeName);
	}

	public static synchronized String getNodeName() {
		ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
		return threadMap.get(threadGroup);
	}
}
