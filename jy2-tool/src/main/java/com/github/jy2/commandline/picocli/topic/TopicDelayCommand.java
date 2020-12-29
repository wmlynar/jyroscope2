package com.github.jy2.commandline.picocli.topic;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.commandline.picocli.topic.completion.TopicNameCompletionCandidates;

import go.jyroscope.ros.std_msgs.Header;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "delay", description = "Print delay of the message time with respect to curent time")
public class TopicDelayCommand implements Runnable {

	@ParentCommand
	TopicCommand parent;

	@Parameters(index = "0", description = "Name of the topic", completionCandidates = TopicNameCompletionCandidates.class)
	String topicName;

	public void run() {
		System.out.println("Subscribed to topic: " + topicName);
		System.out.println("Press Crtl-C to stop");
		Class type = Main.introspector.getTopicType(topicName);
		TopicEchoCommand.subscriber = Main.di.createSubscriber(topicName, type, 5, 50);
		TopicEchoCommand.subscriber.addMessageListener(new Consumer<Object>() {

			private Field timeField = null;
			private Field stampField = null;
			private Field headerField = null;

			@Override
			public void accept(Object t) {
				if (timeField == null && stampField == null && headerField==null) {
					if (timeField == null) {
						try {
							timeField = t.getClass().getDeclaredField("time");
						} catch (NoSuchFieldException | SecurityException e) {
							System.out.println("No field \"time\" in message");
						}
					}
					if (stampField == null) {
						try {
							stampField = t.getClass().getDeclaredField("stamp");
						} catch (NoSuchFieldException | SecurityException e) {
							System.out.println("No field \"stamp\" in message");
						}
					}
					if (headerField == null) {
						try {
							headerField = t.getClass().getDeclaredField("header");
						} catch (NoSuchFieldException | SecurityException e) {
							System.out.println("No field \"header\" in message");
						}
					}
				}
				if (timeField == null && stampField == null && headerField==null) {
					return;
				}
				double messagetimeSeconds = 0;
				try {
					if(headerField!=null) {
						Header h = (Header) headerField.get(t);
						messagetimeSeconds = h.toSeconds();
					}
					if (timeField != null) {
						messagetimeSeconds = timeField.getDouble(t);
					} else if (stampField != null) {
						messagetimeSeconds = stampField.getDouble(t);
					}
				} catch (IllegalArgumentException | IllegalAccessException ex) {
					System.out.println("Problem obtaining field in message");
					return;
				}
				// double nowSeconds = TimeSource.now();
				double nowSeconds = System.currentTimeMillis() * 1e-3;
				double deltaMillis = (nowSeconds - messagetimeSeconds) * 1000;
				System.out.printf("Message delayed by %.3fms\n", deltaMillis);
			}
		});
	}
}
