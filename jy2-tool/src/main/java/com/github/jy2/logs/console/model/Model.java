package com.github.jy2.logs.console.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import go.jyroscope.ros.rosgraph_msgs.Log;

public class Model {

	public ArrayList<Node> nodeList = new ArrayList<>();
	public HashMap<String, Node> nodeMap = new HashMap<>();

	public void add(Log log) {
		Node node = getOrCreate(log.name);
		node.add(log);
	}

	public synchronized Node getNode(int i) {
		if (i >= nodeList.size()) {
			return null;
		}
		return nodeList.get(i);
	}

	public int getNumNodes() {
		return nodeList.size();
	}

	public synchronized void clear() {
		nodeList.clear();
		nodeMap.clear();
	}

	private synchronized Node getOrCreate(String name) {
		Node o = nodeMap.get(name);
		if (o == null) {
			o = new Node();
			o.name = name;
			nodeMap.put(name, o);
			nodeList.add(o);
			Collections.sort(nodeList);
		}
		return o;
	}

//	public synchronized ArrayList<Node> getNodeList() {
//		ArrayList<Node> nodes = new ArrayList<>();
//		nodes.addAll(nodeList);
//		return nodes;
//	}
//
//	public ArrayList<Log> getLogsForNodes() {
//		ArrayList<Log> list = new ArrayList<>();
//		int s = getNumNodes();
//		for (int i = 0; i < s; i++) {
//			list.add(getNode(i).lastEntry);
//		}
//		return list;
//	}

}
