package com.github.jy2.di.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;

import com.github.core.JyroscopeCore;
import com.github.jy2.Subscriber;
import com.github.jy2.api.LogSeldom;
import com.github.jy2.util.ExceptionUtil;

public class SubscriberRef {

	private Object object;
	private Method method;
	private Log log;
	private int queueSize;
	private int timeout;
	private int maxExecutionTime;
	private boolean logStoppedReceivingMessage;
	private Subscriber<?> subscriber;

	public SubscriberRef(JyroscopeCore jy2, Object object, Method method, String topicName, Class<?> topicType,
			int queueSize, int timeout, int maxExecutionTime, boolean logStoppedReceivingMessage, LogSeldom log) {
		this.object = object;
		this.method = method;
		this.log = log;
		this.queueSize = queueSize;
		this.timeout = timeout;
		this.maxExecutionTime = maxExecutionTime;
		this.logStoppedReceivingMessage = logStoppedReceivingMessage;
		this.subscriber = jy2.createSubscriber(topicName, topicType, queueSize);
	}

	public void start() {
		this.subscriber.addMessageListener(new Consumer() {
			@Override
			public void accept(Object message) {
				try {
					method.invoke(object, message);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					ExceptionUtil.rethrowErrorIfCauseIsError(e);
					Throwable t = ExceptionUtil.getCauseIfInvocationException(e);
					log.error("Exception caught while handling message in method " + method.toGenericString() + ", message: "
							+ message, t);
				}
			}
		}, queueSize, timeout, maxExecutionTime, logStoppedReceivingMessage, method);
	}

	public void shutdown() {
		this.subscriber.removeAllMessageListeners();
	}
}
