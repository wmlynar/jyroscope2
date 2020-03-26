package com.github.jy2.di.example;

import com.github.jy2.di.annotations.Subscribe;

import go.jyroscope.ros.nav_msgs.OccupancyGrid;

public class DemoMap2 {

	@Subscribe("/map")
	private void handleLong(OccupancyGrid msg) {
		System.out.println("map2");
	}

}
