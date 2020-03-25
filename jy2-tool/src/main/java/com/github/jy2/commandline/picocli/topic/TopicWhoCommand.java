package com.github.jy2.commandline.picocli.topic;

import java.util.ArrayList;
import java.util.HashSet;

import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.commandline.picocli.topic.completion.TopicNameCompletionCandidates;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "who", description = "List of members who publish to the topic")
public class TopicWhoCommand implements Runnable {

	@ParentCommand
	TopicCommand parent;

	@Parameters(index = "0", description = "Name of the topic", completionCandidates = TopicNameCompletionCandidates.class)
	String topicName;

	@Option(names = { "--waitTime" }, description = "Define how long to wait for transforms in seconds")
	double waitTime = 2;

	public void run() {
		System.out.println("Pausing for " + waitTime + " second(s) to collect publishers to topic");
		HashSet<String> set = Main.introspector.getPublishingMembers(topicName, (long) (waitTime * 1000), null);
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
