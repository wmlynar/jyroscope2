package com.github.jy2.log;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.github.jy2.JyroscopeCore;

import go.jyroscope.ros.rosgraph_msgs.Log;

public class RosoutHandler extends Handler {
	
	private static final String MY_NAME = RosoutHandler.class.getName();

	@Override
	public void close() throws SecurityException {
	}

	@Override
	public void flush() {
	}

	@Override
	public void publish(LogRecord record) {
		// check if logging is not caused by logger - in that case do nothing
		StackTraceElement[] locations = Thread.currentThread().getStackTrace();
		for (int i = 3; i < locations.length; i++) {
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
		String method = record.getSourceMethodName();
		// put line number inside sequence instead of parameters
//		int line = 0;
//		Object[] p = record.getParameters();
//		if (p!=null  && p.length > 0 && p[0] instanceof Integer) {
//			line = (int) p[0];
//		}
		int line;
		long seq = record.getSequenceNumber();
		if (seq < 0) {
			line = (int) -seq;
		} else {
			line = 0;
		}
		String msg = record.getMessage();
		byte roslevel = toRosLevel(record.getLevel());
		if (ex == null) {
			publisher.publish(roslevel, nodeName, cname, method, line, msg);
		} else {
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
