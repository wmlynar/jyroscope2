package com.github.jy2.di.example;

import com.github.jy2.Publisher;
import com.github.jy2.di.annotations.Init;
import com.github.jy2.di.annotations.Publish;
import com.github.jy2.di.annotations.Repeat;
import com.github.jy2.di.annotations.Subscribe;

import go.jyroscope.ros.std_msgs.EmptyMessage;

public class DemoLazy {

	@Publish(value = "/long", lazy = true)
	private Publisher<Long> publisher;

	@Init
	public void init() {
		System.out.println("init");
	}

	@Subscribe("/create")
	public void create(EmptyMessage empty) {
		publisher.publish(System.nanoTime());
	}

}
