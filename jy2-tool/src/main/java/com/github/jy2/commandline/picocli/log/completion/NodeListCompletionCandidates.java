package com.github.jy2.commandline.picocli.log.completion;

import java.util.ArrayList;
import java.util.Iterator;

import com.github.jy2.commandline.picocli.Main;

import picocli.AutoComplete;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;

public class NodeListCompletionCandidates implements Iterable<String> {

	@Override
	public Iterator<String> iterator() {
		if (AutoComplete.argIndex != getNodeNameIndex()) {
			return new ArrayList<String>().iterator();
		}
		
		ArrayList<String> list = Main.introspector.getNodeList();
		list.sort(String::compareToIgnoreCase);
		return list.iterator();
	}

	private int getNodeNameIndex() {
		if (AutoComplete.tentativeMatch == null) {
			return -1;
		}
		int i = 0;
		for (Object obj : AutoComplete.tentativeMatch) {
			if (obj instanceof CommandSpec) { // subcommand
			} else if (obj instanceof OptionSpec) { // option
				OptionSpec opt = (OptionSpec) obj;
				if ("--node".equals(opt.longestName())) {
					return i;
				}
			} else if (obj instanceof PositionalParamSpec) { // positional
			}
			i++;
		}
		return -1;
	}
}
