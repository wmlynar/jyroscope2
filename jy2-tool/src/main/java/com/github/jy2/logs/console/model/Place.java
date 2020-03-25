package com.github.jy2.logs.console.model;

import java.util.ArrayList;

import com.github.jy2.logs.console.collections.LimitedList;
import com.github.jy2.logs.console.utils.LogStringUtils;

import go.jyroscope.ros.rosgraph_msgs.Log;

public class Place implements Comparable<Place> {

	public String file;
	public int line;

	public LimitedList<Log> entries = new LimitedList<>(20);

	public synchronized void add(Log item) {
		entries.add(item);
	}

	public synchronized void getEntries(ArrayList<Log> out) {
		out.clear();
		for (int i = 0; i < entries.size(); i++) {
			out.add(entries.get(i));
		}
	}

	public int getNumEntries() {
		return entries.size();
	}

	public Log getEntry(int i) {
		return entries.get(i);
	}

	@Override
	public int compareTo(Place o) {
		int i = LogStringUtils.nullSafeStringComparator(file, o.file);
		if (i == 0) {
			return line - o.line;
		} else {
			return i;
		}
	}

	@Override
	public String toString() {
		return file + ":" + line;
	}

}
