package com.jyroscope.roscore.parameters;

import com.github.jy2.ParameterListener;

public class ParameterId {

	public String key;
	public ParameterListener consumer;

	public ParameterId(String key, ParameterListener consumer) {
		super();
		this.key = key;
		this.consumer = consumer;
	}

}
