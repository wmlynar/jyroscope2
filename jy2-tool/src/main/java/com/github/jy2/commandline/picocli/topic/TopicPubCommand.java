package com.github.jy2.commandline.picocli.topic;

import com.github.jy2.Publisher;
import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.commandline.picocli.topic.completion.TopicMessageCompletionCandidates;
import com.github.jy2.commandline.picocli.topic.completion.TopicNameCompletionCandidates;
import com.github.jy2.commandline.picocli.topic.completion.TopicTypeCompletionCandidates;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "pub", description = "Publish message to topic")
public class TopicPubCommand implements Runnable {

	public static PublisherThread thread = null;

	@ParentCommand
	TopicCommand parent;

	@Parameters(index = "0", description = "Name of the topic", completionCandidates = TopicNameCompletionCandidates.class)
	String topicName;

	@Parameters(index = "1", description = "Type of the topic", completionCandidates = TopicTypeCompletionCandidates.class)
	String topicType;

	@Parameters(index = "2", description = "Message to be sent", completionCandidates = TopicMessageCompletionCandidates.class)
	String message;

	@Option(names = { "--latched" }, description = "Whether to set publisher in latched mode")
	boolean latched;

	@Option(names = { "--rate" }, description = "Rate in Hz to keep sending the messages")
	Double rate = null;

	public void run() {
		System.out.println("Publishing to topic: " + topicName + ", message: " + message);
		Class<?> type;
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			if (classLoader != null) {
				type = Class.forName(topicType, false, classLoader);
			} else {
				type = Class.forName(topicType);
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		Publisher publisher = Main.di.createPublisher(topicName, type, latched);

		if (rate == null) {
			// if type is primitive - convert to primitive
			// if type is string - publish string
			// if type is object - deserialize from json
			publisher.publish(Main.deserializer.deserialize(type, message));
		} else {
			System.out.println("Press Crtl-C to stop");
			thread = new PublisherThread(publisher, Main.deserializer.deserialize(type, message));
			thread.start();
		}
	}
	
	public final class PublisherThread extends Thread {
		private final Publisher<Object> publisher;
		private final Object obj;
		
		public boolean stop = false;

		private PublisherThread(Publisher<Object> publisher, Object obj) {
			this.publisher = publisher;
			this.obj = obj;
		}

		@Override
		public void run() {
			try {
				while (!stop) {
					publisher.publish(obj);
					synchronized(this) {
						wait((long) (1000 / rate));
					}
				}
			} catch (InterruptedException e) {
			}
		}
	}

}
