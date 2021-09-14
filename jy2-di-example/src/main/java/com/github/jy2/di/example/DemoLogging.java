package com.github.jy2.di.example;

import java.util.function.Consumer;

import org.apache.commons.logging.Log;

import com.github.jy2.Publisher;
import com.github.jy2.Subscriber;
import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.di.annotations.Init;
import com.github.jy2.di.annotations.Inject;
import com.github.jy2.di.annotations.Publish;
import com.github.jy2.di.annotations.Repeat;
import com.github.jy2.di.annotations.Subscribe;

import go.jyroscope.ros.std_msgs.EmptyMessage;

public class DemoLogging {
	
	private Log log = JyroscopeDi.getLog();
	
	@Inject
	JyroscopeDi di;
	
	@Publish("/topic")
	Publisher<EmptyMessage> topicPublisher;

	Subscriber<EmptyMessage> topicSubscriber2;
	
	@Subscribe("/topic")
	public void subscriber1(EmptyMessage empty) {
		log.info("Subscriber through annotation");
	}
	
	@Init
	public void init() {
		topicSubscriber2 = di.createSubscriber("/topic", EmptyMessage.class);
		topicSubscriber2.addMessageListener(t -> log.info("Subscriber through lambda"));
		
		topicSubscriber2.addMessageListener(new Consumer<EmptyMessage>() {
			@Override
			public void accept(EmptyMessage t) {
				log.info("Subscriber through interface");
			}
		});
	}

	@Repeat(interval = 1000)
	public void repeat() {
		topicPublisher.publish(new EmptyMessage());
	}
	

}
