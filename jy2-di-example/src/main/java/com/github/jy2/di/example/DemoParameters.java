package com.github.jy2.di.example;

import com.github.jy2.di.annotations.Parameter;
import com.github.jy2.di.annotations.Repeat;

public class DemoParameters {

	@Parameter("parameter")
	private String parameter2 = "aaa";

	@Repeat(interval = 1000)
	public void repeat() {
		System.out.println("parameter:" + parameter2);
	}
}
