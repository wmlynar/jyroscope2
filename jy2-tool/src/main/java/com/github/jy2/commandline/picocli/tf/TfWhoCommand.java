package com.github.jy2.commandline.picocli.tf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Predicate;

import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.commandline.picocli.tf.completion.TfChildPositionalCompletionCandidates;
import com.github.jy2.commandline.picocli.tf.completion.TfParentPositionalCompletionCandidates;

import go.jyroscope.ros.geometry_msgs.TransformStamped;
import go.jyroscope.ros.tf2_msgs.TFMessage;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "who", description = "List of members who publish to the topic")
public class TfWhoCommand implements Runnable {

	@ParentCommand
	TfCommand parent;

	@Parameters(index = "0", description = "Parent frame id", completionCandidates = TfParentPositionalCompletionCandidates.class)
	String parentFrameId;

	@Parameters(index = "1", description = "Child frame id", completionCandidates = TfChildPositionalCompletionCandidates.class)
	String childFrameId;

	@Option(names = { "--waitTime" }, description = "Define how long to wait for transforms in seconds")
	double waitTime = 1.5;

	public void run() {
		System.out.println("Pausing for " + waitTime + " second(s) to collect transform publishers");
		Predicate<Object> predicate = msg -> {
			boolean match = false;
			TFMessage list = (TFMessage) msg;
			for (TransformStamped t : list.transforms) {
				if (parentFrameId.equals(t.header.frameId) && childFrameId.equals(t.childFrameId)) {
					match = true;
					break;
				}
			}
			return match;
		};
		HashSet<String> set = Main.introspector.getPublishingMembers("/tf", (long) (waitTime * 1000), predicate);
		for (String m : set) {
			System.out.println("\t" + m);

			ArrayList<String> list2 = Main.introspector.getMemberNodeList(m);
			list2.sort(String::compareToIgnoreCase);

			for (String s2 : list2) {
				System.out.println("\t\t" + s2);
			}
		}
	}
}
