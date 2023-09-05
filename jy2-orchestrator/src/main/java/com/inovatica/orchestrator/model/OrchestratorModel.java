package com.inovatica.orchestrator.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class OrchestratorModel {

	public ArrayList<OrchestratorModelItem> items = new ArrayList<>();
	public HashMap<String, OrchestratorModelItem> map = new HashMap<>();

	public String launchFileExtension;
	public String ros2launchFileExtension;
	public String jarFileExtension;
	public String launchFileDirLocal;
	public String ros2LaunchFileDirLocal;
	public String hzLaunchFileDirLocal;
	public String jy2LaunchFileDirLocal;
	public String jarFileDirLocal;
	public String bashFileDirLocal;
	public boolean debug;
	public String javaOpts;
	public String jarParams;
	public String hostName;
	public boolean jmx;
	public String bashParams;
	public File workingDir;
	public int nextDebugPort;
	public int nextJmxPort;
	public int nextKillMe;
	public boolean heapDumpOnOutOfMemomry;
	public String heapDumpPath;
	public boolean shenandoahGc;
	public boolean concurrentGc;
	public boolean optimizeGc;
	public boolean preallocateGc;
	public boolean killOnOutOfMemory;
	public boolean allowChangingNice;
	public boolean logGc;
	public String logGcPath;

	public synchronized OrchestratorModelItem getByName(String string) {
		return map.get(string);
	}

	public synchronized ArrayList<OrchestratorModelItem> items() {
		return items;
	}

	public synchronized void add(OrchestratorModelItem item) {
		if (map.get(item.name) != null) {
			throw new RuntimeException("Item already exists: " + item.name);
		}
		items.add(item);
		map.put(item.name, item);
	}
	
	public synchronized boolean containsItemWithName(String name) {
		return map.containsKey(name);
	}

}
