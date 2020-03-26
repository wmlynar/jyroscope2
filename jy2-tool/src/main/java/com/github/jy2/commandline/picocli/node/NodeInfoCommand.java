package com.github.jy2.commandline.picocli.node;

import java.util.Collection;

import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.commandline.picocli.node.completion.NodeNameCompletionCandidates;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "info", description = "Print information about node")
public class NodeInfoCommand implements Runnable {

	@ParentCommand
	NodeCommand parent;

	@Parameters(index = "0", description = "Name of the node", completionCandidates = NodeNameCompletionCandidates.class)
	String nodeName;

	public void run() {
		if (!Main.introspector.nodeExists(nodeName)) {
			System.out.println("Node " + nodeName + " does not exist");
			return;
		}
		System.out.println("List of publishers:");
		Collection<String> list = Main.introspector.getPublishersForNode(nodeName);
		for (String s : list) {
			System.out.println("\t" + s);
		}
		System.out.println("List of subscribers:");
		list = Main.introspector.getSubscribersForNode(nodeName);
		for (String s : list) {
			System.out.println("\t" + s);
		}
	}
}
