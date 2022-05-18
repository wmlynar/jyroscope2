package com.github.jy2.log;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;

import com.github.jy2.di.LogSeldom;

/**
 * Wrapper around org.apache.commons.logging.Log that sends the log to /rosout
 * topic.
 */
public class Jy2DiLog implements Log, LogSeldom {

	private static final int LOG_SELDOM_PERIOD_MILLISECONDS = 10000;

	private Logger logger;

	/**
	 * Hashmap keeping the information which class at which line logged at what
	 * time.
	 */
	private HashMap<String, HashMap<Integer, LogTime>> logTimeClassesMap = new HashMap<>();

	public Jy2DiLog(String name) {
		logger = Logger.getLogger(name);
	}

	public Jy2DiLog(Class<?> clazz) {
		logger = Logger.getLogger(clazz.getCanonicalName());
	}

	@Override
	public boolean isDebugEnabled() {
		return logger.isLoggable(Level.FINE);
	}

	@Override
	public boolean isErrorEnabled() {
		return logger.isLoggable(Level.SEVERE);
	}

	@Override
	public boolean isFatalEnabled() {
		return logger.isLoggable(Level.SEVERE);
	}

	@Override
	public boolean isInfoEnabled() {
		return logger.isLoggable(Level.INFO);
	}

	@Override
	public boolean isTraceEnabled() {
		return logger.isLoggable(Level.FINEST);
	}

	@Override
	public boolean isWarnEnabled() {
		return logger.isLoggable(Level.WARNING);
	}

	@Override
	public void trace(Object message) {
		if (message instanceof Throwable) {
			log(Level.FINEST, "Exception caught (incorrect use of logging, missing message)", (Throwable) message, false);
		} else {
			log(Level.FINEST, message, null, false);
		}
	}

	@Override
	public void trace(Object message, Throwable t) {
		log(Level.FINEST, message, t, false);
	}

	@Override
	public void debug(Object message) {
		if (message instanceof Throwable) {
			log(Level.FINE, "Exception caught (incorrect use of logging, missing message)", (Throwable) message, false);
		} else {
			log(Level.FINE, message, null, false);
		}
	}

	@Override
	public void debug(Object message, Throwable t) {
		log(Level.FINE, message, t, false);
	}

	@Override
	public void info(Object message) {
		if (message instanceof Throwable) {
			log(Level.INFO, "Exception caught (incorrect use of logging, missing message)", (Throwable) message, false);
		} else {
			log(Level.INFO, message, null, false);
		}
	}

	@Override
	public void info(Object message, Throwable t) {
		log(Level.INFO, message, t, false);
	}

	@Override
	public void warn(Object message) {
		if (message instanceof Throwable) {
			log(Level.WARNING, "Exception caught (incorrect use of logging, missing message)", (Throwable) message, false);
		} else {
			log(Level.WARNING, message, null, false);
		}
	}

	@Override
	public void warn(Object message, Throwable t) {
		log(Level.WARNING, message, t, false);
	}

	@Override
	public void error(Object message) {
		if (message instanceof Throwable) {
			log(Level.SEVERE, "Exception caught (incorrect use of logging, missing message)", (Throwable) message, false);
		} else {
			log(Level.SEVERE, message, null, false);
		}
	}

	@Override
	public void error(Object message, Throwable t) {
		log(Level.SEVERE, message, t, false);
	}

	@Override
	public void fatal(Object message) {
		if (message instanceof Throwable) {
			log(Level.SEVERE, "Exception caught (incorrect use of logging, missing message)", (Throwable) message, false);
		} else {
			log(Level.SEVERE, message, null, false);
		}
	}

	@Override
	public void fatal(Object message, Throwable t) {
		log(Level.SEVERE, message, t, false);
	}

	@Override
	public void debugSeldom(Object message) {
		if (message instanceof Throwable) {
			log(Level.FINE, "Exception caught (incorrect use of logging, missing message)", (Throwable) message, true);
		} else {
			log(Level.FINE, message, null, true);
		}
	}

	@Override
	public void debugSeldom(Object message, Throwable t) {
		log(Level.FINE, message, t, true);
	}

	@Override
	public void errorSeldom(Object message) {
		if (message instanceof Throwable) {
			log(Level.SEVERE, "Exception caught (incorrect use of logging, missing message)", (Throwable) message, true);
		} else {
			log(Level.SEVERE, message, null, true);
		}
	}

	@Override
	public void errorSeldom(Object message, Throwable t) {
		log(Level.SEVERE, message, t, true);
	}

	@Override
	public void fatalSeldom(Object message) {
		if (message instanceof Throwable) {
			log(Level.SEVERE, "Exception caught (incorrect use of logging, missing message)", (Throwable) message, true);
		} else {
			log(Level.SEVERE, message, null, true);
		}
	}

	@Override
	public void fatalSeldom(Object message, Throwable t) {
		log(Level.SEVERE, message, t, true);
	}

	@Override
	public void infoSeldom(Object message) {
		if (message instanceof Throwable) {
			log(Level.INFO, "Exception caught (incorrect use of logging, missing message)", (Throwable) message, true);
		} else {
			log(Level.INFO, message, null, true);
		}
	}

	@Override
	public void infoSeldom(Object message, Throwable t) {
		log(Level.INFO, message, t, true);
	}

	@Override
	public void traceSeldom(Object message) {
		if (message instanceof Throwable) {
			log(Level.FINEST, "Exception caught (incorrect use of logging, missing message)", (Throwable) message, true);
		} else {
			log(Level.FINEST, message, null, true);
		}
	}

	@Override
	public void traceSeldom(Object message, Throwable t) {
		log(Level.FINEST, message, t, true);
	}

	@Override
	public void warnSeldom(Object message) {
		if (message instanceof Throwable) {
			log(Level.WARNING, "Exception caught (incorrect use of logging, missing message)", (Throwable) message, true);
		} else {
			log(Level.WARNING, message, null, true);
		}
	}

	@Override
	public void warnSeldom(Object message, Throwable t) {
		log(Level.WARNING, message, t, true);
	}

	private void log(Level level, Object message, Throwable ex, boolean seldom) {
		if (!logger.isLoggable(level)) {
			return;
		}
		String msg = String.valueOf(message);
		// faster than Thread.currentThread().getStackTrace()
		StackTraceElement locations[] = new Throwable().getStackTrace();
		// Caller will be the third element
		String cname = "unknown";
		String method = "unknown";
		int line = -1;
		if (locations != null && locations.length > 2) {
			StackTraceElement caller = locations[2];
			cname = caller.getClassName();
			method = caller.getMethodName();
			line = caller.getLineNumber();
		}
		if (seldom && line >= 0 && shouldOmitMessage(cname, line)) {
			return;
		}
		LogRecord rec = new LogRecord(level, msg);
		rec.setSourceClassName(cname);
		rec.setSourceMethodName(method);
		rec.setThrown(ex);
		if (RosoutHandler.LINE_NUMBER_INSIDE_PARAMETERS) {
			rec.setParameters(new Object[] { line });
		} else {
			rec.setSequenceNumber(-line);
		}
		logger.log(rec);
	}

	private boolean shouldOmitMessage(String cname, int line) {
		HashMap<Integer, LogTime> logTimeLinesMap = logTimeClassesMap.get(cname);
		if (logTimeLinesMap == null) {
			logTimeLinesMap = new HashMap<>();
			// not worried about multithreading, because in the worst case it will cause
			// logging the message many times
			logTimeClassesMap.put(cname, logTimeLinesMap);
		}

		long time = System.currentTimeMillis();
		LogTime lastTime = logTimeLinesMap.get(line);
		if (lastTime == null) {
			lastTime = new LogTime();
			// again not worried about multithreading, because in the worst case it will
			// cause logging the message many times
			logTimeLinesMap.put(line, lastTime);
		}
		long dt = time - lastTime.time;

		if (dt < LOG_SELDOM_PERIOD_MILLISECONDS) {
			return true;
		}

		lastTime.time = time;

		return false;
	}

}
