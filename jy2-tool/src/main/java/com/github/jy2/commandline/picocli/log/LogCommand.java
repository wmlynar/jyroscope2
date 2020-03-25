package com.github.jy2.commandline.picocli.log;

import com.github.jy2.commandline.picocli.HzCommand;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "log", description = "Log operations", subcommands = { LogEchoCommand.class,
		LogListCommand.class, LogClearCommand.class }, sortOptions = true)
public class LogCommand implements Runnable {

	@ParentCommand
	HzCommand parent;

	public void run() {
		parent.out.println(new CommandLine(this).getUsageMessage());
	}
}
