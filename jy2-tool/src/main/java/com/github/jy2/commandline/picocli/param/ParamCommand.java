package com.github.jy2.commandline.picocli.param;

import com.github.jy2.commandline.picocli.HzCommand;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "param", description = "Parameter operations (new line like in bash ^V^J)", subcommands = { ParamGetCommand.class,
		ParamListCommand.class, ParamSetCommand.class, ParamGetAllCommand.class,
		ParamShowYamlCommand.class, ParamShowJsonCommand.class, ParamDeleteCommand.class }, sortOptions = true)
public class ParamCommand implements Runnable {

	@ParentCommand
	HzCommand parent;

	public void run() {
		parent.out.println(new CommandLine(this).getUsageMessage());
	}
}
