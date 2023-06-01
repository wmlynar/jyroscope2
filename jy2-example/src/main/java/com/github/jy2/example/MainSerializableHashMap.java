package com.github.jy2.example;

import java.util.HashMap;
import java.util.Random;
import java.util.function.Consumer;

import com.github.core.JyroscopeCore;
import com.github.jy2.Publisher;
import com.github.jy2.Subscriber;

public class MainSerializableHashMap {

	public static void main(String[] args) throws InterruptedException {
//		RosTypeConverters.scanAnnotationsAndInitialize();
		JyroscopeCore jy2 = new JyroscopeCore();
		jy2.addRemoteMaster("http://localhost:11311", "localhost", "/jy2" + new Random().nextInt());

		Subscriber<HashMap> subscriber = jy2.createSubscriber("/serializable", HashMap.class);
		subscriber.addMessageListener(new Consumer<HashMap>() {
			@Override
			public void accept(HashMap t) {
				System.out.println("received: " + t.toString());
			}
		});
		
		
		Publisher<HashMap> publisher = jy2.createPublisher("/serializable", HashMap.class, false);
		HashMap<String, String> obj = new HashMap<>();
		obj.put("aaa", "bbb");
//		Thread.sleep(1000);
//		publisher.publish(obj);
		while(true) {
			publisher.publish(obj);
			Thread.sleep(1000);
		}
	}

}
