package com.github.jy2.log;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.github.jy2.JyroscopeCore;
import com.github.jy2.internal.StackTracePrinter;

import go.jyroscope.ros.rosgraph_msgs.Log;

public class RosoutHandler extends Handler {

	private static final String MY_NAME = RosoutHandler.class.getName();

	public static final boolean LINE_NUMBER_INSIDE_PARAMETERS = false;
	public static final boolean PLACE_FROM_EXCEPTION = true;
	public static final boolean STACKTRACE_IN_PARAMETERS = false;

	@Override
	public void close() throws SecurityException {
	}

	@Override
	public void flush() {
	}

	@Override
	public void publish(LogRecord record) {
		// check if logging is not caused by logger - in that case do nothing
		// faster than Thread.currentThread().getStackTrace()
		StackTraceElement locations[] = new Throwable().getStackTrace();
		for (int i = 2; i < locations.length; i++) {
			StackTraceElement l = locations[i];
			if (l.getClassName().equals(MY_NAME)) {
				return;
			}
		}
		RosoutPublisher publisher = JyroscopeCore.ROSOUT_PUBLISHER;
		if (publisher == null) {
			return;
		}
		String nodeName = NodeNameManager.getNodeName();
		Throwable ex = record.getThrown();
		String cname = record.getSourceClassName();
		if (STACKTRACE_IN_PARAMETERS) {
			Object[] p = record.getParameters();
			if (p != null && p.length > 0) {
				cname = (String) p[0];
			}
		}
		String method = record.getSourceMethodName();
		int line;
		if (LINE_NUMBER_INSIDE_PARAMETERS) {
			// put line number inside parameters
			Object[] p = record.getParameters();
			if (p != null && p.length > 0 && p[0] instanceof Integer) {
				line = (int) p[0];
			} else {
				line = 0;
			}
		} else {
			// put line number inside sequence instead of parameters
			long seq = record.getSequenceNumber();
			if (seq < 0) {
				line = (int) -seq;
			} else {
				line = 0;
			}
		}
		String msg = record.getMessage();
		byte roslevel = toRosLevel(record.getLevel());
		if (ex == null) {
			publisher.publish(roslevel, nodeName, cname, method, line, msg);
		} else {
			if (PLACE_FROM_EXCEPTION) {
				StackTraceElement[] exLocations = ex.getStackTrace();
				if (exLocations != null && exLocations.length > 0) {
					cname = exLocations[0].getClassName();
					method = exLocations[0].getMethodName();
					line = exLocations[0].getLineNumber();
					if (STACKTRACE_IN_PARAMETERS) {
						cname = StackTracePrinter.getStackTrace(locations, 4);
					}
				}
			}
			publisher.publish(roslevel, nodeName, cname, method, line, msg, ex);
		}
	}

	private byte toRosLevel(Level level) {
		if (level.equals(Level.FINEST)) {
			return Log.DEBUG;
		} else if (level.equals(Level.FINER)) {
			return Log.DEBUG;
		} else if (level.equals(Level.FINE)) {
			return Log.DEBUG;
		} else if (level.equals(Level.INFO)) {
			return Log.INFO;
		} else if (level.equals(Level.WARNING)) {
			return Log.WARN;
		} else if (level.equals(Level.SEVERE)) {
			return Log.ERROR;
		}
		return Log.FATAL;
	}

}
