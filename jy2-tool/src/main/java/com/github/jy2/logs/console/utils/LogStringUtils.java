package com.github.jy2.logs.console.utils;

public class LogStringUtils {

	public static String trim(String input, int len) {
		int index = input.indexOf("\n");
		String str;
		if (index >= 0) {
			str = input.substring(0, index);
		} else {
			str = input;
		}
		str = str.substring(0, Math.min(str.length(), len));
		if (str.length() != input.length()) {
			str += "...";
		}
		return str;
	}

	public static String pad(String str, int i) {
		int len = str.length();
		if (len < i) {
			StringBuffer sb = new StringBuffer();
			sb.append(str);
			for (int j = len; j < i; j++) {
				sb.append(" ");
			}
			return sb.toString();
		}
		return str.substring(0, i);
	}

	public static String shortClassName(String str) {
		int pos = str.lastIndexOf('.');
		if (pos >= 0) {
			return str.substring(pos, str.length() - 1);
		}
		return str;
	}

	public static int nullSafeStringComparator(final String one, final String two) {
		if (one == null ^ two == null) {
			return (one == null) ? -1 : 1;
		}

		if (one == null && two == null) {
			return 0;
		}

		return one.compareTo(two);
	}
}
