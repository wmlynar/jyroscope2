package com.github.jy2.commandline.picocli.topic;

import java.util.ArrayList;

import com.github.jy2.commandline.picocli.Main;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "list", description = "Print list of topics")
public class TopicListCommand implements Runnable {

	@ParentCommand
	TopicCommand parent;

	@Option(names = { "--grep" }, description = "Character sequence to be expected in topic name")
	String grep;

	public void run() {
		ArrayList<String> list = Main.introspector.getTopicList();
		list.sort(String::compareToIgnoreCase);
		for (String s : list) {
			if (grep != null) {
				if (!s.contains(grep)) {
					continue;
				}
			}
			System.out.println("\t" + s);
		}
	}
}
