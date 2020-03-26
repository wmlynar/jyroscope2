package com.jyroscope.util;

public class Template {

	/*
	 * The template parameters are one-indexed (not zero-indexed)
	 */
	public static String apply(StringBuilder buffer, String template, String... parameters) {
		StringBuilder result = new StringBuilder();
		String temp = null;
		for (int i = 0; i < template.length(); i++) {
			char current = template.charAt(i);
			if (current == '#' && i < template.length() - 1) {
				i++;
				current = template.charAt(i);
				if (current == '#') {
					result.append(current);
				} else if (current == 't') {
					if (temp == null) {
						temp = Id.generate();
					}
					result.append(temp);
				} else if (current == '=') {
					buffer.append(result);
					result = new StringBuilder();
				} else if (current >= '1' && current <= '9') {
					int index = current - '1'; // one-indexed
					result.append(parameters[index]);
				} else
					throw new IllegalArgumentException("Unrecognized character after #");
			} else {
				result.append(current);
			}
		}
		return result.toString();
	}
	
}
