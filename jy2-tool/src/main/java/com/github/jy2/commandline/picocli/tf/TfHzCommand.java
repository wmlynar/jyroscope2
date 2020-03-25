package com.github.jy2.commandline.picocli.tf;

import java.util.function.Consumer;

import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.commandline.picocli.tf.completion.TfChildPositionalCompletionCandidates;
import com.github.jy2.commandline.picocli.tf.completion.TfParentPositionalCompletionCandidates;
import com.github.jy2.commandline.picocli.topic.TopicEchoCommand;

import go.jyroscope.ros.geometry_msgs.TransformStamped;
import go.jyroscope.ros.tf2_msgs.TFMessage;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "hz", description = "Print delay of the transform with respect to curent time")
public class TfHzCommand implements Runnable {

	@ParentCommand
	TfCommand parent;

	@Parameters(index = "0", description = "Parent frame id", completionCandidates = TfParentPositionalCompletionCandidates.class)
	String parentFrameId;

	@Parameters(index = "1", description = "Child frame id", completionCandidates = TfChildPositionalCompletionCandidates.class)
	String childFrameId;

	@Option(names = { "--period" }, description = "Measurement period in seconds")
	double periodSeconds = 1;

	@SuppressWarnings("unchecked")
	public void run() {
		System.out.printf("Subscribed to %s->%s, minimal measurement period: %.3fs\n", parentFrameId, childFrameId,
				periodSeconds);
		System.out.println("Press Crtl-C to stop");
		TopicEchoCommand.subscriber = Main.di.createSubscriber("/tf", TFMessage.class);
		TopicEchoCommand.subscriber.addMessageListener(new Consumer<TFMessage>() {

			private volatile int counter = 0;
			private volatile double startTime = -1;
			private volatile double prevTime = -1;
			private volatile double minPeriod = Double.MAX_VALUE;
			private volatile double maxPeriod = Double.MIN_VALUE;

			@Override
			public synchronized void accept(TFMessage t) {
				try {
					if (parentFrameId == null || childFrameId == null) {
						return;
					}
					for (TransformStamped tr : t.transforms) {
						if (!tr.header.frameId.equals(parentFrameId)) {
							continue;
						}
						if (!tr.childFrameId.equals(childFrameId)) {
							continue;
						}
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
									frequency, invFrequency, minPeriod * 1000.0, maxPeriod * 1000.0, counter);
							counter = 0;
							startTime = time;
							minPeriod = Double.MAX_VALUE;
							maxPeriod = Double.MIN_VALUE;
						}
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
	}
}
