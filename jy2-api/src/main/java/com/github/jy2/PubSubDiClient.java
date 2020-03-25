package com.github.jy2;

public interface PubSubDiClient extends PubSubClient {

	<T> T create(Class<T> clazz);
	<T> T create(Class<T> clazz, String instanceName);
	<T> T create(Class<T> clazz, boolean singleton);
	<T> T create(Class<T> clazz, String instanceName, boolean singleton);

	<T> T inject(T object);
	<T> T inject(T object, String instanceName);
	<T> T inject(T object, boolean singleton);
	<T> T inject(T object, String instanceName, boolean singleton);
	
	void start();
}
