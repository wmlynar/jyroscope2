package com.github.jy2.commandline.picocli.member;

import com.github.jy2.commandline.picocli.HzCommand;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "member", description = "Member operations", subcommands = { MemberListCommand.class,
		MemberInfoCommand.class, MemberKillCommand.class, MemberKillallCommand.class, MemberKilllocalCommand.class,
		MemberStacktraceCommand.class, MemberPingCommand.class, MemberCleanupCommand.class }, sortOptions = true)
public class MemberCommand implements Runnable {

	@ParentCommand
	HzCommand parent;

	public void run() {
		parent.out.println(new CommandLine(this).getUsageMessage());
	}
}
