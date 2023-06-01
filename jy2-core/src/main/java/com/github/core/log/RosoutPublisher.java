package com.github.core.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

import com.github.core.JyroscopeCore;
import com.github.jy2.Publisher;

import go.jyroscope.ros.rosgraph_msgs.Log;
import go.jyroscope.ros.std_msgs.Header;

/**
 * Publisher to /rosout topic that is used by JyroscopeDi logger wrappers.
 */
public class RosoutPublisher {

	public final static String LOGGING_TOPIC = "/rosout";

	private Publisher<Log> publisher;

	public RosoutPublisher(JyroscopeCore jy2) {
		this.publisher = jy2.createPublisher(LOGGING_TOPIC, Log.class);
	}

	public void publish(byte level, String nodeName, String sourceClass, String sourceMethod, int line, Object message,
			Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		throwable.printStackTrace(printWriter);
		publish(level, nodeName, sourceClass, sourceMethod, line, message.toString() + '\n' + stringWriter.toString());
	}

	public void publish(byte level, String nodeName, String sourceClass, String sourceMethod, int line, Object message) {
		Log logMessage = new Log();
		logMessage.header = new Header();
		logMessage.header.stamp = Instant.now();
		logMessage.header.seq = 0;
		logMessage.header.frameId = "";
		logMessage.level = level;
		logMessage.name = nodeName;
		logMessage.msg = message.toString();
		logMessage.file = sourceClass;
		logMessage.function = sourceMethod;
		logMessage.line = line;
		logMessage.topics = new String[0];
		publisher.publish(logMessage);
	}
}
