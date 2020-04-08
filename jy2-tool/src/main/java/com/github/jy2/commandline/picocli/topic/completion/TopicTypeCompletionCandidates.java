package com.github.jy2.commandline.picocli.topic.completion;

import java.util.ArrayList;
import java.util.Iterator;

import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.introspection.TopicInfo;

import picocli.AutoComplete;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;

public class TopicTypeCompletionCandidates implements Iterable<String> {

	public static String topicName;

	@Override
	public Iterator<String> iterator() {
		if (AutoComplete.argIndex != getTopicTypeIndex()) {
			return new ArrayList<String>().iterator();
		}
		
		ArrayList<String> list = new ArrayList<>();
		String topicName = getTopicName();
		if (topicName != null) {
			Class<?> type = Main.introspector.getTopicType(topicName);
			TopicInfo ti = Main.introspector.getTopicInfo(topicName);
			if (ti.reportedJavaType != null) {
				try {
					ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
					if (classLoader != null) {
						type = Class.forName(ti.reportedJavaType, false, classLoader);
					} else {
						type = Class.forName(ti.reportedJavaType);
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			if (type != null) {
				list.add(type.getCanonicalName());
			}
		}
		list.sort(String::compareToIgnoreCase);
		return list.iterator();
	}

	private int getTopicTypeIndex() {
		if (AutoComplete.tentativeMatch == null) {
			return -1;
		}
		int i = 0;
		for (Object obj : AutoComplete.tentativeMatch) {
			if (obj instanceof CommandSpec) { // subcommand
			} else if (obj instanceof OptionSpec) { // option
			} else if (obj instanceof PositionalParamSpec) { // positional
				PositionalParamSpec pos = (PositionalParamSpec) obj;
				if (pos.index().min == 1) {
					return i;
				}
			}
			i++;
		}
		return -1;
	}

	private String getTopicName() {
		if (AutoComplete.tentativeMatch == null) {
			return null;
		}
		for (Object obj : AutoComplete.tentativeMatch) {
			if (obj instanceof CommandSpec) { // subcommand
			} else if (obj instanceof OptionSpec) { // option
			} else if (obj instanceof PositionalParamSpec) { // positional
				PositionalParamSpec pos = (PositionalParamSpec) obj;
				if (pos.index().min == 0) {
					String value = pos.stringValues().get(0);
					return value;
				}
			}
		}
		return null;
	}
}
