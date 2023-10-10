package com.github.jy2.log;

public class NodeNameManager {

    private static final InheritableThreadLocal<String> threadLocal = new InheritableThreadLocal<>();

	public static synchronized void setNodeName(String nodeName) {
		threadLocal.set(nodeName);
	}

	public static synchronized String getNodeName() {
		String name = threadLocal.get();
		return (name == null) ? "UNKNOWN" : name;
	}

}
