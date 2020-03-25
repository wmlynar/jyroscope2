package com.github.jy2.logs.console.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.github.jy2.logs.console.utils.LogLevel;
import com.github.jy2.logs.console.utils.LogLevelUtils;
import com.github.jy2.logs.console.utils.LogStringUtils;

import go.jyroscope.ros.rosgraph_msgs.Log;

public class Node implements Comparable<Node> {

	public String name;

	public ArrayList<Level> levelList = new ArrayList<>();
	public HashMap<LogLevel, Level> levelMap = new HashMap<>();

	public void add(Log log) {
		Level level = getOrCreate(LogLevelUtils.fromRosLevel(log.level));
		level.add(log);
	}

	public int getNumLevels() {
		return levelList.size();
	}

	public synchronized Level getLevel(int i) {
		if (i >= levelList.size()) {
			return null;
		}
		return levelList.get(i);
	}

	@Override
	public int compareTo(Node o) {
		return LogStringUtils.nullSafeStringComparator(name, o.name);
	}

	private synchronized Level getOrCreate(LogLevel level) {
		Level o = levelMap.get(level);
		if (o == null) {
			o = new Level();
			o.level = level;
			levelMap.put(level, o);
			levelList.add(o);
			Collections.sort(levelList);
		}
		return o;
	}

//	public Log lastEntry;
//	public HashMap<LogLevel, Log> lastEntryMap = new HashMap<>();
//
//	public ArrayList<Place> placesList = new ArrayList<>();
//	public HashMap<FileLine, Place> placesMap = new HashMap<>();
//
//	public void add(Log log) {
//		lastEntry = log;
//		lastEntryMap.put(LogLevelUtils.fromRosLevel(log.level), log);
//
//		FileLine fl = new FileLine(log.file, log.line);
//		Place place = placesMap.get(fl);
//		if (name == null) {
//			place = new Place();
//			placesMap.put(fl, place);
//			placesList.add(place);
//			Collections.sort(placesList);
//		}
//		place.add(log);
//		
//	}

}
