package com.github.jy2.commandline.picocli.param;

import java.io.IOException;

import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.commandline.picocli.param.completion.ParameterNameCompletionCandidates;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "get", description = "Print parameter value on screen")
public class ParamGetCommand implements Runnable {

	@ParentCommand
	ParamCommand parent;

	@Parameters(index = "0", description = "Name of the parameter", completionCandidates = ParameterNameCompletionCandidates.class)
	String parameterName;

	public void run() {
		Object paramValue;
		try {
			paramValue = Main.di.getParameterClient().getParameter(parameterName);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		if (paramValue == null) {
			System.out.println("Parameter not set");
		} else {
			System.out.println(paramValue.toString());
		}
	}
}
