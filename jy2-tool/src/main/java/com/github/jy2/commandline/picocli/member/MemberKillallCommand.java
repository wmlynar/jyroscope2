package com.github.jy2.commandline.picocli.member;

import java.util.ArrayList;

import com.github.jy2.Publisher;
import com.github.jy2.commandline.picocli.Main;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "killall", description = "Kill all members except self")
public class MemberKillallCommand implements Runnable {

	@ParentCommand
	MemberCommand parent;

	public void run() {
		ArrayList<String> list = Main.introspector.getMemberList();
		for (String m : list) {
			if (m.equals(Main.di.getMemberName())) {
				continue;
			}
			System.out.println("Killing member: " + m);
			Publisher<String> pub = Main.di.createPublisher("/operations" + m.replace('-', '_'),
					String.class);
			pub.publish("kill");
		}
	}
}
