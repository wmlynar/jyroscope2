package com.github.jy2.commandline.picocli.topic.completion;

import java.util.ArrayList;
import java.util.Iterator;

import com.github.jy2.commandline.picocli.Main;

import picocli.AutoComplete;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;

public class TopicNameCompletionCandidates implements Iterable<String> {

	@Override
	public Iterator<String> iterator() {
		if (AutoComplete.argIndex != getTopicNameIndex()) {
			return new ArrayList<String>().iterator();
		}
		
		return Main.introspector.getTopicList().iterator();
	}

	private int getTopicNameIndex() {
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
