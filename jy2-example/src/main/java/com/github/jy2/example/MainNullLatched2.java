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

public class MainNullLatched2 {

	public static void main(String[] args) throws InterruptedException {
//		RosTypeConverters.scanAnnotationsAndInitialize();
		JyroscopeCore jy2 = new JyroscopeCore();
		jy2.addRemoteMaster("http://localhost:11311", "localhost", "/jy2" + new Random().nextInt());

		Subscriber<PoseWithCovarianceStamped> subscriber = jy2.createSubscriber("/initialpose", PoseWithCovarianceStamped.class);
		subscriber.addMessageListener(new Consumer<PoseWithCovarianceStamped>() {
			@Override
			public void accept(PoseWithCovarianceStamped t) {
				System.out.println("received: " + t.header.toSeconds());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		});

	}


}
