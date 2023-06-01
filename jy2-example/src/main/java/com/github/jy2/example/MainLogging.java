package com.github.jy2.example;

import java.io.IOException;
import java.util.Random;
import java.util.function.Consumer;

import com.github.core.JyroscopeCore;
import com.github.jy2.Subscriber;
import com.github.jy2.api.LogSeldom;

import go.jyroscope.ros.rosgraph_msgs.Log;

public class MainLogging {

	public static void main(String[] args) throws InterruptedException, IOException {

		LogSeldom log1 = JyroscopeCore.getLog();
		log1.info("before");

		JyroscopeCore jy2 = new JyroscopeCore();
		jy2.addRemoteMaster("http://localhost:11311", "localhost", "/jy2" + new Random().nextInt());

		jy2.getParameterClient().setParameter("/logging", "com.github.jy2.level=SEVERE");
		Subscriber<Log> subscriber = jy2.createSubscriber("/rosout", Log.class);
		subscriber.addMessageListener(new Consumer<Log>() {
			@Override
			public void accept(Log t) {
				System.out
						.println("received: " + t.name + " " + t.file + " " + t.function + " " + t.line + " " + t.msg);
			}
		});

		LogSeldom log = JyroscopeCore.getLog();
		log.info("aaa");

		try {
			throw new RuntimeException("bbb");
		} catch (Exception e) {
			log.error("ccc", e);
		}
		Thread.sleep(1000);
	}

}
