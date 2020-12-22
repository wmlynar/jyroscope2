package com.github.jy2.di.example;

import com.github.jy2.di.annotations.Repeat;
import com.github.jy2.di.annotations.RosTimeProvider;
import com.github.jy2.di.ros.TimeProvider;

public class DemoTimeProvider {

	@RosTimeProvider
	TimeProvider timeProvider;

	@Repeat(interval = 100)
	public void repeat() {
		System.out.println(timeProvider.now());
	}

}
