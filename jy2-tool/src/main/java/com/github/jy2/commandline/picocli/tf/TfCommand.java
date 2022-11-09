package com.github.jy2.commandline.picocli.tf;

import com.github.jy2.commandline.picocli.HzCommand;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "tf", description = "Transform operations", subcommands = { TfEchoCommand.class, TfListCommand.class,
		TfWhoCommand.class, TfDelayCommand.class, TfHzCommand.class, TfGetCommand.class }, sortOptions = true)
public class TfCommand implements Runnable {

	@ParentCommand
	HzCommand parent;

	public void run() {
		parent.out.println(new CommandLine(this).getUsageMessage());
	}
}
