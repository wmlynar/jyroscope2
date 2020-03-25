package com.github.jy2.commandline.picocli.node;

import java.util.ArrayList;

import com.github.jy2.Publisher;
import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.commandline.picocli.node.completion.NodeNameCompletionCandidates;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "kill", description = "Kill member holding this node")
public class NodeKillCommand implements Runnable {

	@ParentCommand
	NodeCommand parent;

	@Parameters(index = "0", description = "Name of the node", completionCandidates = NodeNameCompletionCandidates.class)
	String nodeName;

	public void run() {
		if (!Main.introspector.nodeExists(nodeName)) {
			System.out.println("Node " + nodeName + " does not exist");
			return;
		}
		ArrayList<String> list = Main.introspector.getMemberList();
		for (String s : list) {
			ArrayList<String> list2 = Main.introspector.getMemberNodeList(s);
			for (String s2 : list2) {
				if (s2.equals(nodeName)) {
					System.out.println("Killing member: " + s);
					Publisher<String> pub = Main.di.createPublisher("/operations" + s.replace('-', '_'), String.class);
					pub.publish("kill");
				}
			}
		}
	}
}
