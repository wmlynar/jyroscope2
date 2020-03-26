package com.github.jy2.di.example;

import com.github.jy2.di.annotations.Init;
import com.github.jy2.di.annotations.Inject;
import com.github.jy2.di.annotations.Subscribe;
import com.github.jy2.introspection.IntrospectionClient;

import go.jyroscope.ros.introspection_msgs.Member;
import go.jyroscope.ros.introspection_msgs.Node;

public class DemoIntrospection {

	@Inject
	IntrospectionClient introspector;
	
	@Subscribe("/introspection")
	private void handleLong(Member member) {
		System.out.println("member: " + member.name);
		if (member.nodes == null || member.nodes.length == 0) {
			return;
		}
		System.out.println("nodes:");
		for (Node n : member.nodes) {
			System.out.println("  " + n.name);
			System.out.println("  publishers:");
			if (n.publishers != null) {
				for (String p : n.publishers) {
					System.out.println("    " + p);
				}
			}
			System.out.println("  subscribers:");
			if (n.subscribers != null) {
				for (String s : n.subscribers) {
					System.out.println("    " + s);
				}
			}
		}
	}
	
	@Init
	public void init() {
		System.out.println(introspector.getTopicList());
		System.out.println(introspector.getTopicType("/rosout"));
		System.out.println(introspector.getNodesPublishingTopic("/rosout"));
		System.out.println(introspector.getNodesSubscribedToTopic("/rosout"));
		System.out.println(introspector.getPublishersForNode("/rosout"));
		System.out.println(introspector.getSubscribersForNode("/rosout"));
		System.out.println(introspector.nodeExists("/rosout"));
		System.out.println(introspector.getMemberAddress("/rosout"));
	}

}
