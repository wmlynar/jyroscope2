package com.github.jy2.commandline.picocli.log;

import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.logs.console.utils.DisplayUtils;
import com.github.jy2.logs.console.utils.LogLevel;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "list", description = "Sorted list of log entries")
public class LogListCommand implements Runnable {

	@ParentCommand
	LogCommand parent;

	@Option(names = { "--level" }, description = "Logging level")
	LogLevel level;

	@Option(names = { "--node" }, description = "Node name")
	String node;

	@Option(names = { "--class" }, description = "Class name")
	String file;

	@Option(names = { "--method" }, description = "Method name")
	String function;

	@Option(names = { "--line" }, description = "Line number")
	int line = -1;

	@Option(names = { "--grep" }, description = "Character sequence to be expected in serialized message content")
	String grep;

	@Option(names = { "--fullMessage" }, description = "Display full message")
	boolean fullMessage = false;

	@Option(names = { "--maxMessages" }, description = "Maximum number of lines from single position")
	int maxMessages = 1;

	public void run() {
		DisplayUtils.display(Main.logCollector.model, fullMessage, maxMessages);
	}
}
