package com.github.jy2.commandline.picocli.orchestrator;

import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.commandline.picocli.orchestrator.completion.OrchestratorNameCompletionCandidates;
import com.github.jy2.orchestrator.OrchestratorClient;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "restart", description = "Restart item")
public class OrchestratorRestartCommand implements Runnable {

	@ParentCommand
	OrchestratorCommand parent;

	@Parameters(index = "0", description = "Name of the item", completionCandidates = OrchestratorNameCompletionCandidates.class)
	String itemName;

	public void run() {
		if (!Main.introspector.nodeExists(Main.orchestratorName)) {
			System.out.println("Orchestrator node " + Main.orchestratorName
					+ " does not exist. Use orchestrator name command to change the name");
			return;
		}
		
		OrchestratorClient.stopItem(Main.orchestratorName, itemName);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		OrchestratorClient.startItem(Main.orchestratorName, itemName);
	}
}
