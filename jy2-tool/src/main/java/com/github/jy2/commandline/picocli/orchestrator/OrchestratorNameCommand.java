package com.github.jy2.commandline.picocli.orchestrator;

import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.commandline.picocli.node.completion.NodeNameCompletionCandidates;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "name", description = "Set name of the orchestrator node to be used")
public class OrchestratorNameCommand implements Runnable {

	@ParentCommand
	OrchestratorCommand parent;

	@Parameters(index = "0", description = "Name of the orchestrator node to be used", completionCandidates = NodeNameCompletionCandidates.class)
	String itemName;

	public void run() {
		Main.orchestratorName = itemName;
	}
}
