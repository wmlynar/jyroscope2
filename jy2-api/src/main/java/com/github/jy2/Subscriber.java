package com.github.jy2;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public interface Subscriber<T> {


	default Object addMessageListener(Consumer<T> consumer) {
		return addMessageListener(consumer, 1, 0, 50, null);
	}

	default Object addMessageListener(Consumer<T> consumer, int queueSize) {
		return addMessageListener(consumer, queueSize, 0, 50, null);
	}

	default Object addMessageListener(Consumer<T> consumer, int queueSize, int timeout) {
		return addMessageListener(consumer, queueSize, timeout, 50, null);
	}

	default Object addMessageListener(Consumer<T> consumer, int queueSize, int timeout, int maxExecutionTime) {
		return addMessageListener(consumer, queueSize, timeout, maxExecutionTime, null);
	}

	Object addMessageListener(Consumer<T> consumer, int queueSize, int timeout, int maxExecutionTime, Method method);

	void removeMessageListener(Object handle);

	void removeAllMessageListeners();

	/**
	 * WARNING: this will only work after message listener was added
	 */
	boolean isLatched();

	/**
	 * WARNING: this will only work after message listener was added
	 */
	String getRemoteJavaType();

	/**
	 * WARNING: this will only work after message listener was added
	 */
	String getRemoteRosType();

	void shutdown();

}
