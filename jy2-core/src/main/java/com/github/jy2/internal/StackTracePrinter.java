package com.github.jy2.internal;

public class StackTracePrinter {

	public static String getStackTrace(StackTraceElement[] stack, int elementsToSkip) {
		String ret = "";
		for (int i = elementsToSkip; i < stack.length; i++) {
			ret += stack[i] + "\n";
		}
		return ret;
	}

}
