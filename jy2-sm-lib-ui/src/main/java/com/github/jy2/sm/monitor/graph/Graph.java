package com.github.jy2.sm.monitor.graph;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;

public class Graph {

	private MutableGraph mutableGraph;
	private HashMap<String, MutableNode> nodesMap = new HashMap<>();

	public Graph() {
		mutableGraph = mutGraph("example1").setDirected(true);
	}

	public void setStartNode(String name) {
		MutableNode n = getNode(name);
	}

	public void addNode(String name, List<String> connections) {
		MutableNode n1 = getNode(name);
		for (String connection : connections) {
			MutableNode n2 = getNode(connection);
			n1.add(Style.lineWidth(2));
			n1.addLink(n2);
		}
	}

	public BufferedImage renderImage(String currentNode) {
		MutableNode n = null;
		if (currentNode != null) {
			n = getNode(currentNode);
			n.add(Style.lineWidth(4));
			n.add(Color.RED);
		}
		BufferedImage image = Graphviz.fromGraph(mutableGraph).render(Format.PNG).toImage();
		//BufferedImage image = Graphviz.fromGraph(mutableGraph).width(1200).render(Format.PNG).toImage();
		if (currentNode != null) {
			n.add(Style.lineWidth(2));
			n.add(Color.BLACK);
		}
		return image;
	}

	private MutableNode getNode(String name) {
		MutableNode node = nodesMap.get(name);
		if (node == null) {
			node = mutNode(name);
			nodesMap.put(name, node);
			mutableGraph.add(node);
		}
		return node;
	}
}
