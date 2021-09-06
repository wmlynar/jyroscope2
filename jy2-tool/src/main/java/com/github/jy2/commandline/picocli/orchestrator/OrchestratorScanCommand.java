package com.github.jy2.commandline.picocli.orchestrator;

import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.commandline.picocli.orchestrator.completion.OrchestratorNameCompletionCandidates;
import com.github.jy2.orchestrator.OrchestratorClient;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "scan", description = "Scan for new items item")
public class OrchestratorScanCommand implements Runnable {

	@ParentCommand
	OrchestratorCommand parent;

	public void run() {
		if (!Main.introspector.nodeExists(Main.orchestratorName)) {
			System.out.println("Orchestrator node " + Main.orchestratorName
					+ " does not exist. Use orchestrator name command to change the name");
			return;
		}

		OrchestratorClient.scan(Main.orchestratorName);
	}
}
