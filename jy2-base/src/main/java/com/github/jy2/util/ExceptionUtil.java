package com.github.jy2.util;

public class ExceptionUtil {

	public static void rethrowErrorIfCauseIsError(Exception e) throws Error {
		Throwable cause = e.getCause();
		if (cause != null && Error.class.isAssignableFrom(cause.getClass())) {
			throw new Error("Re-throwing exception as error", cause);
		}
	}
}
