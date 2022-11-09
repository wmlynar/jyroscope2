package com.github.jy2.di.example;

import com.github.jy2.di.annotations.Repeat;

public class DemoRuntimeException {

	@Repeat(interval = 100)
	public void repeat() {
		throw new RuntimeException("aaa");
	}

}
