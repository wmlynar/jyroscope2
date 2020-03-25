package com.github.jy2.commandline.picocli.node;

import java.util.ArrayList;

import com.github.jy2.commandline.picocli.Main;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "list", description = "List all nodes by name")
public class NodeListCommand implements Runnable {

	@ParentCommand
	NodeCommand parent;

	@Option(names = { "--grep" }, description = "Character sequence to be expected in node name")
	String grep;

	public void run() {
		ArrayList<String> list = Main.introspector.getNodeList();
		list.sort(String::compareToIgnoreCase);
		for (String s : list) {
			if (grep != null && !s.contains(grep)) {
				continue;
			}
			System.out.println("\t" + s);
		}
	}
}
