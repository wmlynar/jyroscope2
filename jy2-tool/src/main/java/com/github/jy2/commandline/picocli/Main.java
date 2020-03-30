package com.github.jy2.commandline.picocli;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Random;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.MaskingCallback;
import org.jline.reader.ParsedLine;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import com.github.jy2.commandline.common.Deserializer;
import com.github.jy2.commandline.common.Serializer;
import com.github.jy2.commandline.picocli.member.MemberPingCommand;
import com.github.jy2.commandline.picocli.topic.TopicEchoCommand;
import com.github.jy2.commandline.picocli.topic.TopicPubCommand;
import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.introspection.IntrospectionClient;
import com.github.jy2.logs.console.collector.LogCollector;

import picocli.CommandLine;
import picocli.shell.jline3.PicocliJLineCompleter;

public class Main {

	public static Object monitor = new Object();
	public static Serializer serializer = new Serializer();
	public static Deserializer deserializer = new Deserializer();
	public static JyroscopeDi di;
	public static LogCollector logCollector;
	public static IntrospectionClient introspector;

	public static void main(String[] args) {
		try {
			// add all jars from ROS_CLASSPATH to classpath
			String classpathVar = System.getenv("ROS_CLASSPATH");
			if (classpathVar != null) {
				String[] classpathJars = classpathVar.split(",");
				
				if(classpathJars.length>0) {
					URL[] urls = new URL[classpathJars.length];
					for (int i = 0; i < classpathJars.length; i++) {
						urls[i] = new URL("file://" + classpathJars[i]);
					}
					URLClassLoader classloader =  new URLClassLoader(urls, Main.class.getClassLoader());
					Thread.currentThread().setContextClassLoader(classloader);
				}
			} else {
				System.out.println("ROS_CLASSPATH environment variable not set");
			}
			
			di = new JyroscopeDi("jy2_command_line_" + Math.abs(new Random().nextLong()), args);
			logCollector = di.create(LogCollector.class);
			introspector = di.inject(new IntrospectionClient(di));
			di.start();

			// set up the completion
			HzCommand commands = new HzCommand();
			CommandLine cmd = new CommandLine(commands);
			Terminal terminal = TerminalBuilder.builder().build();
			LineReader reader = LineReaderBuilder.builder().terminal(terminal)
					.completer(new PicocliJLineCompleter(cmd.getCommandSpec())).parser(new DefaultParser()).build();
			commands.setReader(reader);
			String prompt = "jy2> ";
			String rightPrompt = null;

			// fix parsing negative numbers
			// cmd.setUnmatchedOptionsArePositionalParams(true);
			// FIXME: patched the library CommandLine.run - revert this and fix properly

			// WOJ: parse program arguments in non-console mode, if present
			if (args.length > 0) {
				CommandLine.run(commands, args);
				reader.getTerminal().flush();
				// prevent from exiting when topic echo from command line
				if (TopicEchoCommand.subscriber != null || TopicPubCommand.thread != null || MemberPingCommand.runPing) {
					Thread.sleep(Long.MAX_VALUE);
				}
				System.exit(0);
			}

			// start the shell and process input until the user quits with Ctl-D
			String line;
			while (true) {
				try {
					String prompt1 = prompt;
					if (TopicEchoCommand.subscriber != null || TopicPubCommand.thread != null || MemberPingCommand.runPing) {
						prompt1 = "";
					}
					line = reader.readLine(prompt1, rightPrompt, (MaskingCallback) null, null);
					if (TopicEchoCommand.subscriber != null || TopicPubCommand.thread != null || MemberPingCommand.runPing) {
						// ignore commands when subscribed to topic
						terminal.writer().println("Press Crtl-C to stop");
						continue;
					}
					ParsedLine pl = reader.getParser().parse(line, 0);
					String[] arguments = pl.words().toArray(new String[0]);
					CommandLine.run(commands, arguments);
				} catch (UserInterruptException e) {
					if (TopicEchoCommand.subscriber != null) {
						TopicEchoCommand.subscriber.removeAllMessageListeners();
						TopicEchoCommand.subscriber = null;
					}
					if (TopicPubCommand.thread != null) {
						TopicPubCommand.thread.stop = true;
						TopicPubCommand.thread.interrupt();
						TopicPubCommand.thread = null;
					}
					MemberPingCommand.runPing = false;
					// ignore
				} catch (EndOfFileException e) {
					System.exit(0);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		System.exit(0);
	}
}
