package com.github.jy2.commandline.picocli.param.completion;

import java.util.ArrayList;
import java.util.Iterator;

import com.github.jy2.commandline.picocli.Main;

import picocli.AutoComplete;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;

public class ParameterValueCompletionCandidates implements Iterable<String> {

	@Override
	public Iterator<String> iterator() {
		if (AutoComplete.argIndex != getParameterValueIndex()) {
			return new ArrayList<String>().iterator();
		}

		ArrayList<String> list = new ArrayList<>();
		String name = getParameterName();
		if (name != null) {
			Object param = Main.di.getParameterClient().getParameter(name);
			if(param==null) {
				return list.iterator();
			}
			String value = Main.di.getParameterClient().getParameter(name).toString();
			list.add(value);
			// disabled for now
//			if ("true".equalsIgnoreCase(value)) {
//				list.add("false");
//			} else if ("false".equalsIgnoreCase(value)) {
//				list.add("true");
//			}
		}
		return list.iterator();
	}

	private String getParameterName() {
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

	private int getParameterValueIndex() {
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
}
