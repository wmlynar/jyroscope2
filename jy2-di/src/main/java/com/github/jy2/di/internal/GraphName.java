package com.github.jy2.di.internal;

import java.util.regex.Pattern;

public final class GraphName {

	private static final Pattern VALID_GRAPH_NAME_PATTERN = Pattern.compile("^([\\~\\/A-Za-z][\\w_\\/]*)?$");

	public static void verify(String name) {
		if (!VALID_GRAPH_NAME_PATTERN.matcher(name).matches()) {
			throw new RuntimeException("Invalid ROS graph name: " + name);
		}
	}
}
