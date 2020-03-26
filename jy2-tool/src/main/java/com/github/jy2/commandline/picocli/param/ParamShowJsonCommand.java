package com.github.jy2.commandline.picocli.param;

import com.github.jy2.commandline.picocli.param.completion.ClassNameCompletionCandidates;
import com.github.jy2.di.utils.JsonMapper;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "showJson", description = "Show json representation of a class")
public class ParamShowJsonCommand implements Runnable {

	@ParentCommand
	ParamCommand parent;

	@Parameters(index = "0", description = "Name of the class", completionCandidates = ClassNameCompletionCandidates.class)
	String className;

	public void run() {
		try {
			Class<?> clazz;
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			if (classLoader != null) {
				clazz = Class.forName(className, false, classLoader);
			} else {
				clazz = Class.forName(className);
			}
			Object o = clazz.newInstance();
			String json = JsonMapper.mapWithRuntimeException(o);
			System.out.println(json);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
