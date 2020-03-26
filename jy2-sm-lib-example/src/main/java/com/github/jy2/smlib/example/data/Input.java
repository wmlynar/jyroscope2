package com.github.jy2.smlib.example.data;

import com.github.jy2.di.annotations.Inject;
import com.github.jy2.smlib3.annotations.DontSerialize;

public class Input {

	@Inject
	@DontSerialize
	public Configuration configuration;

	public int inputValue = 1;

}
