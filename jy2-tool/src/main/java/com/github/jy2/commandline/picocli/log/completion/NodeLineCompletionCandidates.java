package com.github.jy2.commandline.picocli.log.completion;

import java.util.ArrayList;
import java.util.Iterator;

import picocli.AutoComplete;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;

public class NodeLineCompletionCandidates implements Iterable<String> {

	@Override
	public Iterator<String> iterator() {
		ArrayList<String> list = new ArrayList<>();
		if (AutoComplete.argIndex != getTfChildIndex() + 1) {
			return list.iterator();
		}
		return list.iterator();
	}

	private int getTfChildIndex() {
		if (AutoComplete.tentativeMatch == null) {
			return -1;
		}
		int i = 0;
		for (Object obj : AutoComplete.tentativeMatch) {
			if (obj instanceof CommandSpec) { // subcommand
			} else if (obj instanceof OptionSpec) { // option
				OptionSpec opt = (OptionSpec) obj;
				if ("--class".equals(opt.longestName())) {
					return i;
				}
			} else if (obj instanceof PositionalParamSpec) { // positional
			}
			i++;
		}
		return -1;
	}
}
