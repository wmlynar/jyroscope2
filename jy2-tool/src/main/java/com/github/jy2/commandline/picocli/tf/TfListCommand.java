package com.github.jy2.commandline.picocli.tf;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.github.jy2.Subscriber;
import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.commandline.picocli.tf.completion.TfChildOptionCompletionCandidates;
import com.github.jy2.commandline.picocli.tf.completion.TfParentOptionCompletionCandidates;
import com.github.jy2.commandline.picocli.tf.format.TfDisplayFormat;
import com.github.jy2.commandline.picocli.tf.format.TfSerializer;
import com.github.jy2.tf.mat.TfManager;

import go.jyroscope.ros.geometry_msgs.TransformStamped;
import go.jyroscope.ros.tf2_msgs.TFMessage;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "list", description = "Print list of transforms on screen")
public class TfListCommand implements Runnable {

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
			subscriber.removeAllMessageListeners();
			System.out.println("List of transforms:");
			ArrayList<TransformStamped> list = tfManager.getTransformList();
			for (TransformStamped t : list) {
				if (parentFrameId != null) {
					if (!t.header.frameId.equals(parentFrameId)) {
						continue;
					}
				}
				if (childFrameId != null) {
					if (!t.childFrameId.equals(childFrameId)) {
						continue;
					}
				}
				if (grep != null) {
					if (!t.header.frameId.contains(grep) && !t.childFrameId.contains(grep)) {
						continue;
					}
				}
				// System.out.println("\t'" + Main.serializer.serialize(t) + "'");
				System.out.println("\t" + TfSerializer.serialize(t, format));
			}
		} finally {
			if (subscriber != null) {
				subscriber.removeAllMessageListeners();
			}
		}
	}
}
