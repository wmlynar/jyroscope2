package com.github.jy2.example;

import java.util.Random;
import java.util.function.Consumer;

import com.github.core.JyroscopeCore;
import com.github.jy2.Publisher;
import com.github.jy2.Subscriber;

import go.jyroscope.ros.geometry_msgs.PoseWithCovarianceStamped;
import go.jyroscope.ros.std_msgs.Header;

public class MainInitialpose {

	public static void main(String[] args) throws InterruptedException {
//		RosTypeConverters.scanAnnotationsAndInitialize();
		JyroscopeCore jy2 = new JyroscopeCore();
		jy2.addRemoteMaster("http://localhost:11311", "localhost", "/jy2" + new Random().nextInt());

		Publisher<PoseWithCovarianceStamped> publisher0 = jy2.createPublisher("/initialpose", PoseWithCovarianceStamped.class, true);
		PoseWithCovarianceStamped msg0 = new PoseWithCovarianceStamped();
		msg0.header = new Header();
		msg0.header.setSeconds(0);
		publisher0.publish(msg0);
		
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
