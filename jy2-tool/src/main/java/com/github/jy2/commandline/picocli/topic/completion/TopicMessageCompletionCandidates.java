package com.github.jy2.commandline.picocli.topic.completion;

import java.util.ArrayList;
import java.util.Iterator;

import com.github.jy2.commandline.picocli.Main;

import picocli.AutoComplete;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;

public class TopicMessageCompletionCandidates implements Iterable<String> {

	@Override
	public Iterator<String> iterator() {
		if (AutoComplete.argIndex != getTopicMessageIndex()) {
			return new ArrayList<String>().iterator();
		}
		
		ArrayList<String> list = new ArrayList<>();
		String topicType = getTopicType();
		if (topicType != null && !topicType.isEmpty()) {
			try {
				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
				Class<?> clazz;
				if (classLoader != null) {
					clazz = Class.forName(topicType, false, classLoader);
				} else {
					clazz = Class.forName(topicType);
				}
				Object obj = clazz.newInstance();
				list.add("'" + Main.serializer.serialize(obj) + "'");
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		list.sort(String::compareToIgnoreCase);
		return list.iterator();
	}

	private int getTopicMessageIndex() {
		if (AutoComplete.tentativeMatch == null) {
			return -1;
		}
		int i = 0;
		for (Object obj : AutoComplete.tentativeMatch) {
			if (obj instanceof CommandSpec) { // subcommand
			} else if (obj instanceof OptionSpec) { // option
			} else if (obj instanceof PositionalParamSpec) { // positional
				PositionalParamSpec pos = (PositionalParamSpec) obj;
				if (pos.index().min == 2) {
					return i;
				}
			}
			i++;
		}
		return -1;
	}

	private String getTopicType() {
		if (AutoComplete.tentativeMatch == null) {
			return null;
		}
		for (Object obj : AutoComplete.tentativeMatch) {
			if (obj instanceof CommandSpec) { // subcommand
			} else if (obj instanceof OptionSpec) { // option
			} else if (obj instanceof PositionalParamSpec) { // positional
				PositionalParamSpec pos = (PositionalParamSpec) obj;
				if (pos.index().min == 1) {
					String value = pos.stringValues().get(0);
					return value;
				}
			}
		}
		return null;
	}

}
