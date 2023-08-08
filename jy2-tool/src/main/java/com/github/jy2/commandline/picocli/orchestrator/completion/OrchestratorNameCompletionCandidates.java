package com.github.jy2.commandline.picocli.orchestrator.completion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeoutException;

import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.orchestrator.OrchestratorClient;

import picocli.AutoComplete;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;

public class OrchestratorNameCompletionCandidates implements Iterable<String> {

	@Override
	public Iterator<String> iterator() {
		if (AutoComplete.argIndex != getItemNameIndex()) {
			return new ArrayList<String>().iterator();
		}

		try {
			ArrayList<String> list = OrchestratorClient.getItemList(Main.orchestratorName);
			list.sort(String::compareToIgnoreCase);
			return list.iterator();
		} catch (TimeoutException e) {
			return new ArrayList<String>().iterator();
		}
	}

	private int getItemNameIndex() {
		if (AutoComplete.tentativeMatch == null) {
			return -1;
		}
		int i = 0;
		for (Object obj : AutoComplete.tentativeMatch) {
			if (obj instanceof CommandSpec) { // subcommand
			} else if (obj instanceof OptionSpec) { // option
			} else if (obj instanceof PositionalParamSpec) { // positional
				PositionalParamSpec pos = (PositionalParamSpec) obj;
				if (pos.index().min == 0) {
					return i;
				}
			}
			i++;
		}
		return -1;
	}
}
