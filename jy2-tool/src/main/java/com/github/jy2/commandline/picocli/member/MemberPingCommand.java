package com.github.jy2.commandline.picocli.member;

import com.github.jy2.commandline.picocli.member.completion.MemberNameCompletionCandidates;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "ping", description = "Print XMLRPC reply time from member")
public class MemberPingCommand implements Runnable {

	@ParentCommand
	MemberCommand parent;

	@Parameters(index = "0", description = "Name of the member", completionCandidates = MemberNameCompletionCandidates.class)
	String memberName;

	public static boolean runPing = false;

	public void run() {
		System.out.println("Pinging member: " + memberName);
		System.out.println("Press Crtl-C to stop");
		runPing = true;
		while(runPing) {
			System.out.println("ping");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}
}
