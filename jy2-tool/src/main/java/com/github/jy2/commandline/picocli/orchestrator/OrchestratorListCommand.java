package com.github.jy2.commandline.picocli.orchestrator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.TimeoutException;

import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.orchestrator.OrchestratorClient;
import com.inovatica.orchestrator.json.OrchestratorStatusItem;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "list", description = "List all items by name")
public class OrchestratorListCommand implements Runnable {

	@ParentCommand
	OrchestratorCommand parent;

	@Option(names = { "--grep" }, description = "Character sequence to be expected in item name")
	String grep;

//	@Option(names = { "--orchestratorName" }, description = "Name of the orchestrator node")
//	String orchestratorName;

	public void run() {
//		String address = orchestratorName != null ? orchestratorName : Main.orchestratorName;

		if (!Main.introspector.nodeExists(Main.orchestratorName)) {
			System.out.println("Orchestrator node " + Main.orchestratorName
					+ " does not exist. Use orchestrator name command to change the name");
			return;
		}
		
		try {
			ArrayList<OrchestratorStatusItem> list = OrchestratorClient.getItemStatuses(Main.orchestratorName);
			list.sort(new Comparator<OrchestratorStatusItem>() {
				@Override
				public int compare(OrchestratorStatusItem o1, OrchestratorStatusItem o2) {
					return o1.name.compareToIgnoreCase(o2.name);
				}
			});
			for (OrchestratorStatusItem s : list) {
				if (grep != null && !s.name.contains(grep)) {
					continue;
				}
				System.out.println("\t" + s.name + (s.isStarted ? "\tstarted" : "\tstopped") + "\tdbg" + s.debugPort
						+ "\tjmx" + s.jmxPort);
			}
		} catch (TimeoutException e) {
			System.out.println(e.getMessage());
		}
	}
}
