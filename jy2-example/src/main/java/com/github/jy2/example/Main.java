package com.github.jy2.example;

import java.util.Random;
import java.util.function.Consumer;

import com.github.core.JyroscopeCore;
import com.github.jy2.Publisher;
import com.github.jy2.Subscriber;

import go.jyroscope.ros.std_msgs.StringMessage;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		//RosTypeConverters.scanAnnotationsAndInitialize();
		JyroscopeCore jy2 = new JyroscopeCore();
		jy2.addRemoteMaster("http://localhost:11311", "localhost", "/jy2" + new Random().nextInt());

//		jy2.createPublisher("/string", StringMessage.class, true);
		Publisher<StringMessage> publisher0 = jy2.createPublisher("/string", StringMessage.class, true);
		StringMessage msg0 = new StringMessage();
		msg0.data = "fsd latched";
		publisher0.publish(msg0);

		Thread.sleep(1000);

		Subscriber<StringMessage> subscriber = jy2.createSubscriber("/string", StringMessage.class);
		subscriber.addMessageListener(new Consumer<StringMessage>() {
			@Override
			public void accept(StringMessage t) {
				System.out.println("received: " + t.data);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		});
		System.out.println("is latched: " + subscriber.isLatched());

		Publisher<StringMessage> publisher = jy2.createPublisher("/string", StringMessage.class, true);
		StringMessage msg = new StringMessage();
		msg.data = "dfgd";
		publisher.publish(msg);

		Thread.sleep(1000);

		System.out.println("subscribers: " + publisher.getNumberOfMessageListeners());
	}

}
