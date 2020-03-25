package com.github.jy2.commandline.picocli.member;

import java.util.ArrayList;

import com.github.jy2.Publisher;
import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.commandline.picocli.member.completion.MemberNameCompletionCandidates;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "stacktrace", description = "Dump member stacktrace to logs")
public class MemberStacktraceCommand implements Runnable {

	@ParentCommand
	MemberCommand parent;

	@Parameters(index = "0", description = "Name of the member", completionCandidates = MemberNameCompletionCandidates.class)
	String memberName;

	public void run() {
		ArrayList<String> list = Main.introspector.getMemberList();
		
		if (!list.contains(memberName)) {
			System.out.println("Member " + memberName + " does not exist");
			return;
		}
		
		System.out.println("Sending stacktrace request to member: " + memberName);
		Publisher<String> pub = Main.di.createPublisher("/operations" + memberName.replace('-', '_'), String.class);
		pub.publish("stacktrace");
	}
}
