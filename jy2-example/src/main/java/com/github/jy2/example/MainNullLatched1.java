package com.github.jy2.example;

import java.util.Random;
import java.util.function.Consumer;

import com.github.jy2.JyroscopeCore;
import com.github.jy2.Publisher;
import com.github.jy2.Subscriber;
import com.github.jy2.mapper.RosTypeConverters;

import go.jyroscope.ros.geometry_msgs.PoseWithCovarianceStamped;
import go.jyroscope.ros.std_msgs.Header;
import go.jyroscope.ros.std_msgs.StringMessage;

public class MainNullLatched1 {

	public static void main(String[] args) throws InterruptedException {
//		RosTypeConverters.scanAnnotationsAndInitialize();
		JyroscopeCore jy2 = new JyroscopeCore();
		jy2.addRemoteMaster("http://localhost:11311", "localhost", "/jy2" + new Random().nextInt());

		Publisher<PoseWithCovarianceStamped> publisher0 = jy2.createPublisher("/initialpose", PoseWithCovarianceStamped.class, true);

		Thread.sleep(100000);
		
	}


}
