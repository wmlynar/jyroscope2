package com.github.jy2.example;

import java.util.Random;

import com.github.core.JyroscopeCore;
import com.github.jy2.Publisher;

import go.jyroscope.ros.sensor_msgs.LaserScan;

public class MainNullFields {

	public static void main(String[] args) throws InterruptedException {
//		RosTypeConverters.scanAnnotationsAndInitialize();
		JyroscopeCore jy2 = new JyroscopeCore();
		jy2.addRemoteMaster("http://localhost:11311", "localhost", "/jy2" + new Random().nextInt());

		Publisher<LaserScan> publisher = jy2.createPublisher("/scan123", LaserScan.class);

		LaserScan msg = new LaserScan();
		publisher.publish(msg);
	}

}
