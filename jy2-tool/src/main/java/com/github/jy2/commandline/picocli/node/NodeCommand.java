package com.github.jy2.commandline.picocli.node;

import com.github.jy2.commandline.picocli.HzCommand;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "node", description = "Node operations", subcommands = { NodeListCommand.class, NodeInfoCommand.class,
		NodeKillCommand.class }, sortOptions = true)
public class NodeCommand implements Runnable {

	@ParentCommand
	HzCommand parent;

	public void run() {
		parent.out.println(new CommandLine(this).getUsageMessage());
	}
}
