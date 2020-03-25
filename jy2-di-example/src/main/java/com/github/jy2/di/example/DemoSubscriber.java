package com.github.jy2.di.example;

import com.github.jy2.di.annotations.Subscribe;

public class DemoSubscriber {

	@Subscribe("/long")
	private void handleLong(Long msg) {
		long time = System.nanoTime() - msg;
		System.out.println(time * 0.000001);
	}

}
