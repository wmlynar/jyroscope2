package com.github.jy2.commandline.picocli.member;

import java.util.ArrayList;

import com.github.jy2.commandline.picocli.Main;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "list", description = "List all members by name")
public class MemberListCommand implements Runnable {

	@ParentCommand
	MemberCommand parent;

	public void run() {
		System.out.println("List of members:");
		ArrayList<String> list = Main.introspector.getMemberList();
		for (String m : list) {
//			System.out.println("\t" + m + " created-by: " + m.getStringAttribute("createdby"));
			System.out.println("\t" + m);

			ArrayList<String> list2 = Main.introspector.getMemberNodeList(m);
			list2.sort(String::compareToIgnoreCase);

			for (String s2 : list2) {
				System.out.println("\t\t" + s2);
			}
		}
	}
}
