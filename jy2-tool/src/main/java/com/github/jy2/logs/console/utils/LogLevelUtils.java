package com.github.jy2.logs.console.utils;

import go.jyroscope.ros.rosgraph_msgs.Log;

public class LogLevelUtils {

	public static byte toRosLevel(LogLevel level) {
		if (level.equals(LogLevel.DEBUG)) {
			return Log.DEBUG;
		} else if (level.equals(LogLevel.INFO)) {
			return Log.INFO;
		} else if (level.equals(LogLevel.WARN)) {
			return Log.WARN;
		} else if (level.equals(LogLevel.ERROR)) {
			return Log.ERROR;
		}
		return Log.FATAL;
	}

	public static LogLevel fromRosLevel(byte level) {
		if (level == Log.DEBUG) {
			return LogLevel.DEBUG;
		} else if (level == Log.INFO) {
			return LogLevel.INFO;
		} else if (level == Log.WARN) {
			return LogLevel.WARN;
		} else if (level == Log.ERROR) {
			return LogLevel.ERROR;
		}
		return LogLevel.FATAL;
	}

}
