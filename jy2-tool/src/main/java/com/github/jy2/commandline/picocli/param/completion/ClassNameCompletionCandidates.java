package com.github.jy2.commandline.picocli.param.completion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.google.common.reflect.ClassPath;

import picocli.AutoComplete;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;

public class ClassNameCompletionCandidates implements Iterable<String> {

	@Override
	public Iterator<String> iterator() {
		ArrayList<String> list = new ArrayList<>();
		if (AutoComplete.argIndex != getClassNameIndex()) {
			return list.iterator();
		}

		// String className = getClassName();
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
				// if (info.getName().startsWith(className)) {
				list.add(info.getName());
				// }
			}
		} catch (IOException e) {
		}

		return list.iterator();
	}

//	private String getClassName() {
//		if (AutoComplete.tentativeMatch == null) {
//			return "";
//		}
//		for (Object obj : AutoComplete.tentativeMatch) {
//			if (obj instanceof CommandSpec) { // subcommand
//			} else if (obj instanceof OptionSpec) { // option
//			} else if (obj instanceof PositionalParamSpec) { // positional
//				PositionalParamSpec pos = (PositionalParamSpec) obj;
//				if (pos.index().min == 0) {
//					String value = pos.stringValues().get(0);
//					return value;
//				}
//			}
//		}
//		return "";
//	}

	private int getClassNameIndex() {
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
