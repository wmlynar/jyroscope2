package com.github.jy2.commandline.picocli.topic;

import java.util.function.Consumer;

import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.commandline.picocli.topic.completion.TopicNameCompletionCandidates;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "hz", description = "Print publishing rate of topics")
public class TopicHzCommand implements Runnable {

	@ParentCommand
	TopicCommand parent;

	@Parameters(index = "0", description = "Name of the topic", completionCandidates = TopicNameCompletionCandidates.class)
	String topicName;

	@Option(names = { "--period" }, description = "Measurement period in seconds")
	double periodSeconds = 1;

	public void run() {
		System.out.printf("Subscribed to topic: %s, minimal measurement period: %.3f s\n", topicName, periodSeconds);
		System.out.println("Press Crtl-C to stop");
		Class type = Main.introspector.getTopicType(topicName);
		TopicEchoCommand.subscriber = Main.di.createSubscriber(topicName, type, 5, 50);
		TopicEchoCommand.subscriber.addMessageListener(new Consumer<Object>() {
			
			private volatile int counter = 0;
			private volatile double startTime = -1;
			private volatile double prevTime = -1;
			private volatile double minPeriod = Double.MAX_VALUE;
			private volatile double maxPeriod = Double.MIN_VALUE;
			
			@Override
			public synchronized void accept(Object t) {
				try {
					double time = System.nanoTime() * 1e-9;
					counter++;
					if (startTime == -1) {
						startTime = time;
						prevTime = startTime;
						counter = 0;
					}
					double delta = time - startTime;
					double period = time - prevTime;
					prevTime = time;
					if (minPeriod > period && counter > 0) {
						minPeriod = period;
					}
					if (maxPeriod < period && counter > 0) {
						maxPeriod = period;
					}
					if (counter > 0 && delta > periodSeconds) {
						double frequency = counter / delta;
						double invFrequency = 1000 * delta / counter;
						System.err.printf("\tRate: %.3fHz Period: %.3fms Min: %.3fms Max: %.3fms Count: %d\n",
								frequency,
								invFrequency, minPeriod * 1000.0, maxPeriod * 1000.0, counter);
						counter = 0;
						startTime = time;
						minPeriod = Double.MAX_VALUE;
						maxPeriod = Double.MIN_VALUE;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
