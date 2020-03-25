package com.github.jy2;

public interface Publisher<T> {

	void publish(T obj);

	int getNumberOfMessageListeners();

	void skipLocalMessages(boolean skip);

}
