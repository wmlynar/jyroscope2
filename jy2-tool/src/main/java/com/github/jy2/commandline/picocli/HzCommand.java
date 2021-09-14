package com.github.jy2.commandline.picocli;

import java.io.PrintWriter;

import org.jline.reader.LineReader;
import org.jline.reader.impl.LineReaderImpl;

import com.github.jy2.commandline.picocli.log.LogCommand;
import com.github.jy2.commandline.picocli.member.MemberCommand;
import com.github.jy2.commandline.picocli.node.NodeCommand;
import com.github.jy2.commandline.picocli.orchestrator.OrchestratorCommand;
import com.github.jy2.commandline.picocli.param.ParamCommand;
import com.github.jy2.commandline.picocli.tf.TfCommand;
import com.github.jy2.commandline.picocli.topic.TopicCommand;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "", description = "Hz tool to operate on hazelcast-di cluster", footer = { "",
		"Press Ctl-D to exit." }, subcommands = { NodeCommand.class, TopicCommand.class, ParamCommand.class,
				TfCommand.class, MemberCommand.class, LogCommand.class, OrchestratorCommand.class }, sortOptions = true)
public class HzCommand implements Runnable {
	LineReaderImpl reader;
	public PrintWriter out;

	public void setReader(LineReader reader) {
		this.reader = (LineReaderImpl) reader;
		out = reader.getTerminal().writer();
	}

	public void run() {
		out.println(new CommandLine(this).getUsageMessage());
	}

}
