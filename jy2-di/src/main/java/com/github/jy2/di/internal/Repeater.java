package com.github.jy2.di.internal;

import java.lang.reflect.Method;

import com.github.jy2.di.annotations.Repeat;
import com.github.jy2.workqueue.RepeaterProcessor3;

public class Repeater {

	public Object object;
	public Method method;
	public Repeat repeat;
	public Thread thread;
	public RepeaterProcessor3 processor;

	/**
	 * Shutdown = true indicates that repeater should be shut down.
	 */
	public boolean shutdown = false;

	public Repeater(Object object, Method method, Repeat parameters) {
		this.object = object;
		this.method = method;
		this.repeat = parameters;
	}

	/**
	 * Shuts down the repeater.
	 */
	public void shutdown() {
		shutdown = true;
		if (thread != null) {
			thread.interrupt();
		} else if (processor != null) {
			processor.stop();
		}

	}
}
