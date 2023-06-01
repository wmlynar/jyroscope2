package com.github.jy2.example;

import java.util.Random;
import java.util.function.Consumer;

import com.github.core.JyroscopeCore;
import com.github.jy2.Publisher;
import com.github.jy2.Subscriber;

public class MainPrimitives {

	public static void main(String[] args) throws InterruptedException {
//		RosTypeConverters.scanAnnotationsAndInitialize();
		JyroscopeCore jy2 = new JyroscopeCore();
		jy2.addRemoteMaster("http://localhost:11311", "localhost", "/jy2" + new Random().nextInt());

		Subscriber<Boolean> subscriber = jy2.createSubscriber("/bool", Boolean.class);
		subscriber.addMessageListener(new Consumer<Boolean>() {
			@Override
			public void accept(Boolean t) {
				System.out.println("received: " + t);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		});

		Publisher<Boolean> publisher = jy2.createPublisher("/bool", Boolean.class, true);
		publisher.publish(false);
	}

}
