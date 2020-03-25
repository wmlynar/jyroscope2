package com.github.jy2.di.example;

import com.github.jy2.Publisher;
import com.github.jy2.di.annotations.Init;
import com.github.jy2.di.annotations.Publish;
import com.github.jy2.di.annotations.Repeat;

public class DemoPublisher {

	@Publish("/long")
	private Publisher<Long> publisher;

	@Init
	public void init() {
		System.out.println("init");
	}

	@Repeat(interval = 100)
	public void repeat() {
//		System.out.println("repeat");
		publisher.publish(System.nanoTime());
	}

}
