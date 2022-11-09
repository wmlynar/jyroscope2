package com.github.jy2.util;

import java.lang.reflect.InvocationTargetException;

public class ExceptionUtil {

	public static void rethrowErrorIfCauseIsError(Exception e) throws Error {
		Throwable cause = e.getCause();
		if (cause != null && Error.class.isAssignableFrom(cause.getClass())) {
			throw new Error("Re-throwing exception as error", cause);
		}
	}

	public static Throwable getCauseIfInvocationException(Exception e) {
		Throwable t;
		if (e instanceof InvocationTargetException && e.getCause() != null) {
			t = e.getCause();
		} else {
			t = e;
		}
		return t;
	}
}
