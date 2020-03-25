package com.github.jy2.example;

import java.util.Random;
import java.util.function.Consumer;

import com.github.jy2.JyroscopeCore;
import com.github.jy2.Publisher;
import com.github.jy2.Subscriber;

public class MainSerializableObjects {

	public static void main(String[] args) throws InterruptedException {
//		RosTypeConverters.scanAnnotationsAndInitialize();
		JyroscopeCore jy2 = new JyroscopeCore();
		jy2.addRemoteMaster("http://localhost:11311", "localhost", "/jy2" + new Random().nextInt());

		Subscriber<SerializableObject> subscriber = jy2.createSubscriber("/serializable", SerializableObject.class);
		subscriber.addMessageListener(new Consumer<SerializableObject>() {
			@Override
			public void accept(SerializableObject t) {
				System.out.println("received: " + t.data);
			}
		});

		Publisher<SerializableObject> publisher = jy2.createPublisher("/serializable", SerializableObject.class, false);
		SerializableObject obj = new SerializableObject();
		obj.data = "aaa";
//		Thread.sleep(1000);
//		publisher.publish(obj);
		while(true) {
			publisher.publish(obj);
			Thread.sleep(1000);
		}
	}

}
