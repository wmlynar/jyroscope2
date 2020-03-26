package com.github.jy2.commandline.picocli.log;

import com.github.jy2.commandline.picocli.Main;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "clear", description = "Clear aggregated logs")
public class LogClearCommand implements Runnable {

	@ParentCommand
	LogCommand parent;

	public void run() {
		Main.logCollector.model.clear();
		System.out.println("Cleared aggregated logs");
	}
}
