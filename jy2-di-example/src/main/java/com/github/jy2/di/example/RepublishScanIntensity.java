package com.github.jy2.di.example;

import com.github.jy2.Publisher;
import com.github.jy2.di.annotations.Publish;
import com.github.jy2.di.annotations.Subscribe;

import go.jyroscope.ros.sensor_msgs.LaserScan;

public class RepublishScanIntensity {
	
	
	@Publish("/scan0")
	Publisher<Double> topicPublisher;
	
	@Subscribe("/scan")
	public void onScan(LaserScan scan) {
		topicPublisher.publish((double)scan.intensities[0]);
	}

}
