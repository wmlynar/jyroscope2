package com.github.jy2.di.internal;

import java.lang.reflect.Field;

public class ParameterFromFileReference {

	public String parameterName;
	public String defaultValue;
	public boolean watch;
	public Object object;
	public Field field;

	public ParameterFromFileReference(String parameterName, String defaultValue, boolean watch, Object object,
			Field field) {
		this.parameterName = parameterName;
		this.defaultValue = defaultValue;
		this.watch = watch;
		this.object = object;
		this.field = field;
	}
}
