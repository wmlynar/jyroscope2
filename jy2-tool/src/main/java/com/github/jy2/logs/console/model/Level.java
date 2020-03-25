package com.github.jy2.logs.console.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.github.jy2.logs.console.utils.LogLevel;
import com.github.jy2.logs.console.utils.LogLevelUtils;

import go.jyroscope.ros.rosgraph_msgs.Log;

public class Level implements Comparable<Level> {

	public LogLevel level;

	public ArrayList<Place> placeList = new ArrayList<>();
	public HashMap<String, Place> placeMap = new HashMap<>();

	public void add(Log log) {
		Place place = getOrCreate(log.file, log.line);
		place.add(log);
	}

	public int getNumPlaces() {
		return placeList.size();
	}

	public synchronized Place getPlace(int i) {
		if (i >= placeList.size()) {
			return null;
		}
		return placeList.get(i);
	}

	private synchronized Place getOrCreate(String file, int line) {
		String hash = file + ":" + line;
		Place o = placeMap.get(hash);
		if (o == null) {
			o = new Place();
			o.file = file;
			o.line = line;
			placeMap.put(hash, o);
			placeList.add(o);
			Collections.sort(placeList);
		}
		return o;
	}

	@Override
	public int compareTo(Level o) {
		return LogLevelUtils.toRosLevel(o.level) - LogLevelUtils.toRosLevel(level);
	}

	@Override
	public String toString() {
		return level.toString();
	}
}
