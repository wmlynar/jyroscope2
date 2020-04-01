package com.github.jy2.commandline.picocli.topic;

import java.util.function.Consumer;

import com.github.jy2.Subscriber;
import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.commandline.picocli.topic.completion.TopicNameCompletionCandidates;
import com.github.jy2.introspection.TopicInfo;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "echo", description = "Print list of topics")
public class TopicEchoCommand implements Runnable {

	@ParentCommand
	TopicCommand parent;

	@Parameters(index = "0", description = "Name of the topic", completionCandidates = TopicNameCompletionCandidates.class)
	String topicName;

	@Option(names = { "--grep" }, description = "Character sequence to be expected in serialized message content")
	String grep;

	public static Subscriber subscriber = null;

	public void run() {
		System.out.println("Subscribed to topic: " + topicName);
		System.out.println("Press Crtl-C to stop");
//		Class type = Main.introspector.getTopicType(topicName);
//		TopicInfo ti = Main.introspector.getTopicInfo(topicName);
//		if (ti.reportedJavaType != null) {
//			try {
//				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//				if (classLoader != null) {
//					type = Class.forName(ti.reportedJavaType, false, classLoader);
//				} else {
//					type = Class.forName(ti.reportedJavaType);
//				}
//			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
//			}
//		}
//		subscriber = Main.di.createSubscriber(topicName, type, 5, 50);
		subscriber = Main.di.createSubscriber(topicName, null, 5, 50);
		subscriber.addMessageListener(new Consumer<Object>() {

			private volatile int counter = 0;

			@Override
			public void accept(Object t) {
				try {
					String s = Main.serializer.serialize(t);
					if (grep != null) {
						if (!s.contains(grep)) {
							return;
						}
					}
					System.out.println("" + counter++ + "\t" + t.getClass().getCanonicalName() + " '" + s + "'");
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
	}
}
