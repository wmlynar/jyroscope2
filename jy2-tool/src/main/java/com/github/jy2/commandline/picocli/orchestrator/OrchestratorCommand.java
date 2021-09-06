package com.github.jy2.commandline.picocli.orchestrator;

import com.github.jy2.commandline.picocli.HzCommand;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "orchestrator", description = "Orchestrator operations", subcommands = { OrchestratorListCommand.class,
		OrchestratorStartCommand.class, OrchestratorStopCommand.class,
		OrchestratorRestartCommand.class }, sortOptions = true)
public class OrchestratorCommand implements Runnable {

	@ParentCommand
	HzCommand parent;

	public void run() {
		parent.out.println(new CommandLine(this).getUsageMessage());
	}
}
