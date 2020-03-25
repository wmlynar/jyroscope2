package com.github.jy2;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public interface Subscriber<T> {


	default Object addMessageListener(Consumer<T> consumer) {
		return addMessageListener(consumer, 1, 0, 50, null);
	}

	default Object addMessageListener(Consumer<T> consumer, int queueLength) {
		return addMessageListener(consumer, queueLength, 0, 50, null);
	}

	default Object addMessageListener(Consumer<T> consumer, int queueLength, int timeout) {
		return addMessageListener(consumer, queueLength, timeout, 50, null);
	}

	default Object addMessageListener(Consumer<T> consumer, int queueLength, int timeout, int maxExecutionTime) {
		return addMessageListener(consumer, queueLength, timeout, maxExecutionTime, null);
	}

	Object addMessageListener(Consumer<T> consumer, int queueLength, int timeout, int maxExecutionTime, Method method);

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

}
