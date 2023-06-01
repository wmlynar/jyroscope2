package com.github.jy2.api;

import org.apache.commons.logging.Log;

/**
 * Adds methods on top of <code>org.apache.commons.logging.Log</code> to log
 * messages only once per 10 seconds.
 *
 */
public interface LogSeldom extends Log {

	void debugSeldom(Object message);

	void debugSeldom(Object message, Throwable t);

	void errorSeldom(Object message);

	void errorSeldom(Object message, Throwable t);

	void fatalSeldom(Object message);

	void fatalSeldom(Object message, Throwable t);

	void infoSeldom(Object message);

	void infoSeldom(Object message, Throwable t);

	void traceSeldom(Object message);

	void traceSeldom(Object message, Throwable t);

	void warnSeldom(Object message);

	void warnSeldom(Object message, Throwable t);

}
