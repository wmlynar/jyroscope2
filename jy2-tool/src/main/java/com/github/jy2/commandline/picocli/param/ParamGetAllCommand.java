package com.github.jy2.commandline.picocli.param;

import java.io.IOException;
import java.util.ArrayList;

import com.github.jy2.commandline.picocli.Main;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "getall", description = "List parameters and their values on screen")
public class ParamGetAllCommand implements Runnable {

	@ParentCommand
	ParamCommand parent;

	@Option(names = { "--grep" }, description = "Character sequence to be expected in parameter name")
	String grep;

	public void run() {
		ArrayList<String> list;
		try {
			list = Main.di.getParameterClient().getParameterNames();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		list.sort(String::compareToIgnoreCase);
		for (String s : list) {
			if (grep != null) {
				if (!s.contains(grep)) {
					continue;
				}
			}
			try {
				Object paramValue = Main.di.getParameterClient().getParameter(s);
				if (paramValue == null) {
					System.out.println("\t" + s + "\t" + "Parameter not set");
				} else {
					System.out.println("\t" + s + "\t" + paramValue.toString());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
