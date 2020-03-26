package com.github.jy2.commandline.picocli.tf;

import java.util.function.Consumer;

import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.commandline.picocli.tf.completion.TfChildOptionCompletionCandidates;
import com.github.jy2.commandline.picocli.tf.completion.TfParentOptionCompletionCandidates;
import com.github.jy2.commandline.picocli.tf.format.TfDisplayFormat;
import com.github.jy2.commandline.picocli.tf.format.TfSerializer;
import com.github.jy2.commandline.picocli.topic.TopicEchoCommand;

import go.jyroscope.ros.geometry_msgs.TransformStamped;
import go.jyroscope.ros.tf2_msgs.TFMessage;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "echo", description = "Print transform on screen")
public class TfEchoCommand implements Runnable {

	@ParentCommand
	TfCommand parent;

	@Option(names = { "--parent" }, description = "Parent frame id", completionCandidates = TfParentOptionCompletionCandidates.class)
	String parentFrameId;

	@Option(names = { "--child" }, description = "Child frame id", completionCandidates = TfChildOptionCompletionCandidates.class)
	String childFrameId;

	@Option(names = { "--grep" }, description = "Character sequence to be expected in parent frame id or child frame id")
	String grep;

	@Option(names = { "--format" }, description = "Format to display the transform")
	TfDisplayFormat format = TfDisplayFormat.rpy;

	@SuppressWarnings("unchecked")
	public void run() {
		System.out.println("Subscribed to topic: /tf");
		System.out.println("Press Crtl-C to stop");
		TopicEchoCommand.subscriber = Main.di.createSubscriber("/tf", TFMessage.class);
		TopicEchoCommand.subscriber.addMessageListener(new Consumer<TFMessage>() {
			@Override
			public void accept(TFMessage t) {
				try {
					for (TransformStamped tr : t.transforms) {
						if (parentFrameId != null) {
							if (!tr.header.frameId.equals(parentFrameId)) {
								continue;
							}
						}
						if (childFrameId != null) {
							if (!tr.childFrameId.equals(childFrameId)) {
								continue;
							}
						}
						if (grep != null) {
							if (!tr.header.frameId.contains(grep) && !tr.childFrameId.contains(grep)) {
								continue;
							}
						}
//						System.out.println("\t'" + Main.serializer.serialize(tr) + "'");
						System.out.println("\t" + TfSerializer.serialize(tr, format));
					}
					
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
	}
}
