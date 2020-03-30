package com.github.jy2.commandline.picocli.member;

import com.github.jy2.commandline.picocli.Main;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "cleanup", description = "Cleaning up stale node registration information on the ROS Master ")
public class MemberCleanupCommand implements Runnable {

	@ParentCommand
	MemberCommand parent;

	public void run() {
		System.out.println("Cleaning up stale members");
		Main.di.getMasterClient().memberCleanup();
	}
}
