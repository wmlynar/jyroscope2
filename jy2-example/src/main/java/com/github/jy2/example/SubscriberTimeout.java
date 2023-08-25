package com.github.jy2.example;

import java.util.Random;
import java.util.function.Consumer;

import com.github.jy2.JyroscopeCore;
import com.github.jy2.Publisher;
import com.github.jy2.Subscriber;

public class SubscriberTimeout {

	public static void main(String[] args) throws InterruptedException {
//		RosTypeConverters.scanAnnotationsAndInitialize();
		JyroscopeCore jy2 = new JyroscopeCore();
		jy2.addRemoteMaster("http://localhost:11311", "localhost", "/jy2" + new Random().nextInt());

		Subscriber<String> subscriber = jy2.createSubscriber("/aaa", String.class);
		subscriber.addMessageListener(new Consumer<String>() {
			@Override
			public void accept(String t) {
				System.out.println("received: " + t);
			}
		}, 5, 100);
	}

}
