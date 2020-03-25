package com.github.jy2.introspection;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.github.jy2.Subscriber;
import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.di.annotations.Repeat;
import com.github.jy2.di.annotations.Subscribe;
import com.github.jy2.mapper.RosTypeConverters;
import com.jyroscope.types.ConversionException;

import go.jyroscope.ros.introspection_msgs.Member;
import go.jyroscope.ros.introspection_msgs.Node;

public class IntrospectionClient {

	private JyroscopeDi jy2;
	private HashMap<String, Member> members = new HashMap<>();
	private Object mutex = new Object();
	private static final long TIMEOUT = 2000;
	private ArrayList<String> tmp = new ArrayList<>();

	public IntrospectionClient(JyroscopeDi hzDi) {
		this.jy2 = hzDi;
	}

	@Subscribe("/introspection")
	public void handleIntrospection(Member member) {
		member.time = System.currentTimeMillis();
		synchronized (mutex) {
			members.put(member.name, member);
		}
	}

	@Repeat(interval = 1000)
	public void repeat() {
		long now = System.currentTimeMillis();
		synchronized (mutex) {
			tmp.clear();
			for (Entry<String, Member> m : members.entrySet()) {
				if (now - m.getValue().time > TIMEOUT) {
					tmp.add(m.getKey());
				}
			}
			for (String s : tmp) {
				members.remove(s);
			}
		}
	}

	public ArrayList<String> getTopicList() {
		ArrayList<String> result = new ArrayList<>();

		ArrayList<ArrayList<String>> rosTopics = jy2.getMasterClient().getTopicTypes();
		for (ArrayList<String> topic : rosTopics) {
			final String topicName = topic.get(0);
//			final String typeName = topic.get(1);
			result.add(topicName);
		}

		return result;
	}

	public Class<?> getTopicType(String topicName) {
		ArrayList<ArrayList<String>> rosTopics = jy2.getMasterClient().getTopicTypes();
		for (ArrayList<String> topic : rosTopics) {
			if (topicName.equals(topic.get(0))) {
				final String typeName = topic.get(1);
				try {
					RosTypeConverters.precompileByRosName(typeName);
				} catch (ConversionException e) {
					e.printStackTrace();
					return null;
				}
				Class<?> clazz = RosTypeConverters.getRosType(typeName);
				return clazz;
			}
		}

		return null;
	}

	public TopicInfo getTopicInfo(String topicName) {
		Subscriber<Object> subscriber = jy2.createSubscriber(topicName, null);
		// one always needs to add message listener to get reported info (for example is
		// latched)
		subscriber.addMessageListener(new Consumer<Object>() {
			@Override
			public void accept(Object arg0) {
			}
		});
		subscriber.removeAllMessageListeners();
		TopicInfo ti = new TopicInfo();
		ti.reportedIsLatched = subscriber.isLatched();
		ti.reportedJavaType = subscriber.getRemoteJavaType();
		ti.reportedRosType = subscriber.getRemoteRosType();
		return ti;
	}

	public Collection<String> getNodesPublishingTopic(String topicName) {

		ArrayList<String> result = new ArrayList<>();

		synchronized (mutex) {

			// add nodes from member list
			for (Entry<String, Member> m : members.entrySet()) {
				for (Node n : m.getValue().nodes) {
					for (String p : n.publishers) {
						if (p.equals(topicName)) {
							result.add(n.name);
							break;
						}
					}
				}
			}

			ArrayList<ArrayList<ArrayList<Object>>> state = jy2.getMasterClient().getSystemState();
			ArrayList<ArrayList<Object>> publishers = state.get(0);
			// ArrayList<ArrayList<Object>> subscribers = state.get(1);
			// ArrayList<ArrayList<Object>> services = state.get(2);
			for (ArrayList<Object> data : publishers) {
				String topic = (String) data.get(0);
				ArrayList<String> nodes = (ArrayList<String>) data.get(1);
				if (topic.equals(topicName)) {
					for (String n : nodes) {
						// add node only if it is not a member
						if (members.get(n) == null) {
							result.add(n);
						}
					}
					return result;
				}
			}
			return result;
		}
	}

	public Collection<String> getNodesSubscribedToTopic(String topicName) {

		ArrayList<String> result = new ArrayList<>();

		synchronized (mutex) {

			// add nodes from member list
			for (Entry<String, Member> m : members.entrySet()) {
				for (Node n : m.getValue().nodes) {
					for (String p : n.subscribers) {
						if (p.equals(topicName)) {
							result.add(n.name);
							break;
						}
					}
				}
			}

			ArrayList<ArrayList<ArrayList<Object>>> state = jy2.getMasterClient().getSystemState();
//			ArrayList<ArrayList<Object>> publishers = state.get(0);
			ArrayList<ArrayList<Object>> subscribers = state.get(1);
//			ArrayList<ArrayList<Object>> services = state.get(2);
			for (ArrayList<Object> data : subscribers) {
				String topic = (String) data.get(0);
				ArrayList<String> nodes = (ArrayList<String>) data.get(1);
				if (topic.equals(topicName)) {
					for (String n : nodes) {
						// add node only if it is not a member
						if (members.get(n) == null) {
							result.add(n);
						}
					}
					return result;
				}
			}
			return result;
		}
	}

	public Collection<String> getPublishersForNode(String nodeName) {
		ArrayList<String> result = new ArrayList<>();

		synchronized (mutex) {

			// add nodes from member list
			for (Entry<String, Member> m : members.entrySet()) {
				for (Node n : m.getValue().nodes) {
					if (nodeName.equals(n.name)) {
						for (String p : n.publishers) {
							result.add(p);
						}
						return result;
					}
				}
			}

			// when not found get publishers from roscore

			ArrayList<ArrayList<ArrayList<Object>>> state = jy2.getMasterClient().getSystemState();
			ArrayList<ArrayList<Object>> publishers = state.get(0);
			// ArrayList<ArrayList<Object>> subscribers = state.get(1);
			// ArrayList<ArrayList<Object>> services = state.get(2);
			for (ArrayList<Object> data : publishers) {
				String topic = (String) data.get(0);
				ArrayList<String> nodes = (ArrayList<String>) data.get(1);
				if (nodes.contains(nodeName)) {
					result.add(topic);
				}
			}
			return result;

		}
	}

	public Collection<String> getSubscribersForNode(String nodeName) {
		ArrayList<String> result = new ArrayList<>();

		synchronized (mutex) {

			// add nodes from member list
			for (Entry<String, Member> m : members.entrySet()) {
				for (Node n : m.getValue().nodes) {
					if (nodeName.equals(n.name)) {
						for (String p : n.subscribers) {
							result.add(p);
						}
						return result;
					}
				}
			}

			// when not found get subscribers from roscore

			ArrayList<ArrayList<ArrayList<Object>>> state = jy2.getMasterClient().getSystemState();
			// ArrayList<ArrayList<Object>> publishers = state.get(0);
			ArrayList<ArrayList<Object>> subscribers = state.get(1);
			// ArrayList<ArrayList<Object>> services = state.get(2);
			for (ArrayList<Object> data : subscribers) {
				String topic = (String) data.get(0);
				ArrayList<String> nodes = (ArrayList<String>) data.get(1);
				if (nodes.contains(nodeName)) {
					result.add(topic);
				}
			}
			return result;
		}
	}

	public HashSet<String> getPublishingMembers(String topicName, long timeoutMilliseconds,
			Predicate<Object> predicate) {
		// TODO
		return new HashSet<>();
	}

	public ArrayList<String> getMemberNodeList(String name) {
		ArrayList<String> list = new ArrayList<String>();
		synchronized (mutex) {
			Member m = members.get(name);
			if (m != null) {
				for (Node n : m.nodes) {
					list.add(n.name);
				}
			}
			return list;
		}
	}

	public boolean nodeExists(String nodeName) {
		synchronized (mutex) {
			// add nodes from member list
			for (Entry<String, Member> m : members.entrySet()) {
				for (Node n : m.getValue().nodes) {
					if (nodeName.equals(n.name)) {
						return true;
					}
				}
			}
		}

		return jy2.getMasterClient().lookupNode(nodeName) != null;
	}

	public ArrayList<String> getMemberList() {

		HashSet<String> nodeSet = new HashSet<>();

		ArrayList<ArrayList<ArrayList<Object>>> state = jy2.getMasterClient().getSystemState();
		ArrayList<ArrayList<Object>> publishers = state.get(0);
		ArrayList<ArrayList<Object>> subscribers = state.get(1);
//		ArrayList<ArrayList<Object>> services = state.get(2);
		for (ArrayList<Object> data : publishers) {
			ArrayList<String> nodes = (ArrayList<String>) data.get(1);
			nodeSet.addAll(nodes);
		}
		for (ArrayList<Object> data : subscribers) {
			ArrayList<String> nodes = (ArrayList<String>) data.get(1);
			nodeSet.addAll(nodes);
		}
		ArrayList<String> result = new ArrayList<>();
		for (String n : nodeSet) {
			result.add(n);
		}
		return result;
	}

	public ArrayList<String> getNodeList() {
		HashSet<String> nodeSet = new HashSet<>();

		ArrayList<ArrayList<ArrayList<Object>>> state = jy2.getMasterClient().getSystemState();
		ArrayList<ArrayList<Object>> publishers = state.get(0);
		ArrayList<ArrayList<Object>> subscribers = state.get(1);
//		ArrayList<ArrayList<Object>> services = state.get(2);
		for (ArrayList<Object> data : publishers) {
			ArrayList<String> nodes = (ArrayList<String>) data.get(1);
			nodeSet.addAll(nodes);
		}

		for (ArrayList<Object> data : subscribers) {
			ArrayList<String> nodes = (ArrayList<String>) data.get(1);
			nodeSet.addAll(nodes);
		}

		ArrayList<String> result = new ArrayList<>();
		for (String n : nodeSet) {
			result.add(n);
		}

		synchronized (mutex) {
			for (Entry<String, Member> m : members.entrySet()) {
				for (Node n : m.getValue().nodes) {
					nodeSet.add(n.name);
				}
			}
		}

		return result;
	}

	public String getMemberCreatedBy(String memberName) {
		// TODO
		return "";
	}

	public InetAddress getMemberAddress(String member) {
		String address = jy2.getMasterClient().lookupNode(member);
		if (address == null) {
			return null;
		}
		try {
			URI uri = new URI(address);
			String host = uri.getHost();
			return InetAddress.getByName(host);
		} catch (UnknownHostException | URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

}
