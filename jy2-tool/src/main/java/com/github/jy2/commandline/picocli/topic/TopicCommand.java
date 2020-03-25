package com.github.jy2.commandline.picocli.topic;

import com.github.jy2.commandline.picocli.HzCommand;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "topic", description = "Topic operations", subcommands = { TopicEchoCommand.class,
		TopicInfoCommand.class, TopicListCommand.class, TopicPubCommand.class,
		TopicWhoCommand.class, TopicHzCommand.class, TopicDelayCommand.class,
		TopicDeleteCommand.class }, sortOptions = true)
public class TopicCommand implements Runnable {

	@ParentCommand
	HzCommand parent;

	public void run() {
		parent.out.println(new CommandLine(this).getUsageMessage());
	}
}
