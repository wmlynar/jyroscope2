package com.github.jy2.commandline.picocli.member;

import java.util.ArrayList;

import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.commandline.picocli.member.completion.MemberNameCompletionCandidates;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "info", description = "Print information about member")
public class MemberInfoCommand implements Runnable {

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
		
		System.out.println("Created by: " + Main.introspector.getMemberCreatedBy(memberName));
		
		list = Main.introspector.getMemberNodeList(memberName);
		list.sort(String::compareToIgnoreCase);
		
		System.out.println("List of nodes:");
		for (String s : list) {
			System.out.println("\t" + s);
		}
	}
}
