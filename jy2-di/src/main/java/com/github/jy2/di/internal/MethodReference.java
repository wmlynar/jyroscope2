package com.github.jy2.di.internal;

import java.lang.reflect.Method;

public class MethodReference {

	public Object object;
	public Method method;
	
	public MethodReference(Object object, Method method) {
		this.object = object;
		this.method = method;
	}
	
}
