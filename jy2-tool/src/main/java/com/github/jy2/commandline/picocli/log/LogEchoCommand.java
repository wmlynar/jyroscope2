package com.github.jy2.commandline.picocli.log;

import java.text.SimpleDateFormat;
import java.util.function.Consumer;

import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.commandline.picocli.topic.TopicEchoCommand;
import com.github.jy2.logs.console.utils.LogLevel;
import com.github.jy2.logs.console.utils.LogLevelUtils;

import go.jyroscope.ros.rosgraph_msgs.Log;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "echo", description = "Print list of topics")
public class LogEchoCommand implements Runnable {

	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");;

	@ParentCommand
	LogCommand parent;

	@Option(names = { "--level" }, description = "Logging level")
	LogLevel level;

	@Option(names = { "--node" }, description = "Node name")
	String node;

	@Option(names = { "--class" }, description = "Class name")
	String file;

	@Option(names = { "--method" }, description = "Method name")
	String function;

	@Option(names = { "--line" }, description = "Line number")
	int line = -1;

	@Option(names = { "--grep" }, description = "Character sequence to be expected in serialized message content")
	String grep;

	@Option(names = { "--fullMessage" }, description = "Display full message")
	boolean fullMessage = false;

	public void run() {
		System.out.println("Subscribed to topic: /rosout");
		System.out.println("Press Crtl-C to stop");
		TopicEchoCommand.subscriber = Main.di.createSubscriber("/rosout", null);
		TopicEchoCommand.subscriber.addMessageListener(new Consumer<Object>() {
			
			private volatile int counter = 0;
			
			@Override
			public void accept(Object t) {
				try {
					Log log = (Log) t;
					if (level != null) {
						int minLevel = LogLevelUtils.toRosLevel(level);
						if (log.level < minLevel) {
							return;
						}
					}
					if (node != null) {
						if (!node.equals(log.name)) {
							return;
						}
					}
					if (file != null) {
						if (!file.equals(log.file)) {
							return;
						}
					}
					if (function != null) {
						if (!function.equals(log.function)) {
							return;
						}
					}
					if (line >= 0) {
						if (line != log.line) {
							return;
						}
					}
					String s = Main.serializer.serialize(log);
					if (grep != null) {
						if (!s.contains(grep)) {
							return;
						}
					}
					// System.out.println("" + counter++ + "\t" + t.getClass().getCanonicalName() +
					// " '" + s + "'");
					String strNode = log.name;
					String strLevel = LogLevelUtils.fromRosLevel(log.level).toString();
					String strPlace = log.file + ":" + log.line;
					String strMethod = log.function;
					String strMsg = log.msg;
//					if (fullMessage) {
					System.out.printf("%s %s %.3f %s %s\n%s\n", strLevel, strNode, log.header.toSeconds(), strPlace, strMethod,
								strMsg);
//					} else {
////						strMsg = LogStringUtils.trim(strMsg, 100);
//						System.out.printf("%s %s %s %s\n", strLevel, strNode, strPlace, strMsg);
//					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
