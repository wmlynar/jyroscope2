package com.github.jy2.commandline.picocli.tf;

import java.util.function.Consumer;

import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.commandline.picocli.tf.completion.TfChildPositionalCompletionCandidates;
import com.github.jy2.commandline.picocli.tf.completion.TfParentPositionalCompletionCandidates;
import com.github.jy2.commandline.picocli.topic.TopicEchoCommand;

import go.jyroscope.ros.geometry_msgs.TransformStamped;
import go.jyroscope.ros.tf2_msgs.TFMessage;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "delay", description = "Print delay of the transform with respect to curent time")
public class TfDelayCommand implements Runnable {

	@ParentCommand
	TfCommand parent;

	@Parameters(index = "0", description = "Parent frame id", completionCandidates = TfParentPositionalCompletionCandidates.class)
	String parentFrameId;

	@Parameters(index = "1", description = "Child frame id", completionCandidates = TfChildPositionalCompletionCandidates.class)
	String childFrameId;

	@SuppressWarnings("unchecked")
	public void run() {
		TopicEchoCommand.subscriber = Main.di.createSubscriber("/tf", TFMessage.class);
		TopicEchoCommand.subscriber.addMessageListener(new Consumer<TFMessage>() {
			@Override
			public void accept(TFMessage t) {
				if (parentFrameId == null || childFrameId == null) {
					return;
				}
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
						// double nowSeconds = TimeSource.now();
						double nowSeconds = System.currentTimeMillis() * 1e-3;
						double deltaMillis = (nowSeconds - tr.header.toSeconds()) * 1000;
						System.out.printf("\t%.3fms\n", deltaMillis);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
