package com.github.jy2.commandline.picocli.topic;

import java.util.Collection;

import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.commandline.picocli.topic.completion.TopicNameCompletionCandidates;
import com.github.jy2.introspection.TopicInfo;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "info", description = "Print information about topic")
public class TopicInfoCommand implements Runnable {

	@ParentCommand
	TopicCommand parent;

	@Parameters(index = "0", description = "Name of the topic", completionCandidates = TopicNameCompletionCandidates.class)
	String topicName;

	public void run() {
		Class<?> type = Main.introspector.getTopicType(topicName);
		String typeName = type == null ? "null" : type.getName();
		System.out.println("Master type: " + typeName);
		TopicInfo ti = Main.introspector.getTopicInfo(topicName);
		System.out.println("Remote type: " + ti.reportedRosType);
		System.out.println("Remote java type: " + ti.reportedJavaType);
		System.out.println("Remote is latched: " + ti.reportedIsLatched);
		System.out.println("List of publishing nodes:");
		Collection<String> list = Main.introspector.getNodesPublishingTopic(topicName);
		for (String s : list) {
			System.out.println("\t" + s);
		}
		System.out.println("List of subscribed nodes:");
		list = Main.introspector.getNodesSubscribedToTopic(topicName);
		for (String s : list) {
			System.out.println("\t" + s);
		}
	}

}
