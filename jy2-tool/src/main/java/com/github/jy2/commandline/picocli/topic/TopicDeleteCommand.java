package com.github.jy2.commandline.picocli.topic;

import com.github.jy2.commandline.picocli.topic.completion.TopicNameCompletionCandidates;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "delete", description = "Delete the topic")
public class TopicDeleteCommand implements Runnable {

	@ParentCommand
	TopicCommand parent;

	@Parameters(index = "0", description = "Name of the topic", completionCandidates = TopicNameCompletionCandidates.class)
	String topicName;

	public void run() {
		System.out.println("Deleting topic :" + topicName);
//		Main.di.deleteTopic(topicName);
	}

}
