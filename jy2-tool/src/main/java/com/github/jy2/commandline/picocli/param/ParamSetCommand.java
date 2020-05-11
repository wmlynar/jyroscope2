package com.github.jy2.commandline.picocli.param;

import java.io.IOException;

import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.commandline.picocli.param.completion.ParameterNameCompletionCandidates;
import com.github.jy2.commandline.picocli.param.completion.ParameterValueCompletionCandidates;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "set", description = "Set parameter value")
public class ParamSetCommand implements Runnable {

	@ParentCommand
	ParamCommand parent;

	@Parameters(index = "0", description = "Name of the parameter", completionCandidates = ParameterNameCompletionCandidates.class)
	String parameterName;

	@Parameters(index = "1", description = "Value of the parameter", completionCandidates = ParameterValueCompletionCandidates.class)
	String parameterValue;

	public void run() {
		try {
			Main.di.getParameterClient().setParameter(parameterName, parameterValue);
			System.out.println("Parameter set succesfully");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
