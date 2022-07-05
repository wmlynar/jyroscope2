package com.github.jy2.commandline.picocli.tf;

import java.util.function.Consumer;

import com.github.jy2.Subscriber;
import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.commandline.picocli.tf.completion.TfChildPositionalCompletionCandidates;
import com.github.jy2.commandline.picocli.tf.completion.TfParentPositionalCompletionCandidates;
import com.github.jy2.commandline.picocli.tf.format.TfDisplayFormat;
import com.github.jy2.commandline.picocli.tf.format.TfSerializer;
import com.github.jy2.tf.mat.TfManager;

import go.jyroscope.ros.geometry_msgs.TransformStamped;
import go.jyroscope.ros.tf2_msgs.TFMessage;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "get", description = "Print value of transform")
public class TfGetCommand implements Runnable {

	@ParentCommand
	TfCommand parent;

	@Parameters(index = "0", description = "Parent frame id", completionCandidates = TfParentPositionalCompletionCandidates.class)
	String parentFrameId;

	@Parameters(index = "1", description = "Child frame id", completionCandidates = TfChildPositionalCompletionCandidates.class)
	String childFrameId;

	@Option(names = { "--format" }, description = "Format to display the transform")
	TfDisplayFormat format = TfDisplayFormat.rpy;

	@Option(names = { "--waitTime" }, description = "Define how long to wait for transforms in seconds")
	double waitTime = 1.5;

	public void run() {
		System.out.println("Pausing for " + waitTime + " second(s) to collect transforms");
		Subscriber<TFMessage> subscriber = null;
		try {
			subscriber = Main.di.createSubscriber("/tf", TFMessage.class);
			TfManager tfManager = new TfManager();
			subscriber.addMessageListener(new Consumer<TFMessage>() {
				@Override
				public void accept(TFMessage t) {
					try {
						tfManager.add(t);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			try {
				// static transforms usually published every 1000ms, so wait 100ms more
				Thread.sleep((long) (waitTime * 1000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// subscriber.removeAllMessageListeners();
			subscriber.shutdown();
			TransformStamped transform = new TransformStamped();
			boolean success = tfManager.getTransformLatest(parentFrameId, childFrameId, transform);
			if (success) {
				System.out.println("\t" + TfSerializer.serialize(transform, format));
			} else {
				System.out.println("\tTransform not found");
			}
		} finally {
			if (subscriber != null) {
				// subscriber.removeAllMessageListeners();
				subscriber.shutdown();
			}
		}
	}
}
