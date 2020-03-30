package com.jyroscope.ros.master;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;

import com.github.jy2.MasterClient;
import com.github.jy2.di.LogSeldom;
import com.github.jy2.log.Jy2DiLog;
import com.jyroscope.ros.RosSlave;
import com.jyroscope.server.xmlrpc.XMLRPCArray;
import com.jyroscope.server.xmlrpc.XMLRPCClient;
import com.jyroscope.server.xmlrpc.XMLRPCException;

public class RosMasterClient implements MasterClient {

	private final static LogSeldom LOG = new Jy2DiLog(RosMasterClient.class);

	private final RosSlave slave;

	public RosMasterClient(RosSlave slave) {
		this.slave = slave;
	}

	@Override
	public String lookupNode(String node) {
		try {
			XMLRPCClient master = new XMLRPCClient(slave.getMasterURI());
			Object result = master.call("lookupNode", new XMLRPCArray(new Object[] { slave.getCallerId(), node }));
			XMLRPCArray resultList = (XMLRPCArray) result;
			if ((Integer) resultList.get(0) != 1) {
				return null;
			} else {
				return (String) resultList.get(2);
			}
		} catch (IOException | XMLRPCException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ArrayList<ArrayList<String>> getTopicTypes() {
		try {
			XMLRPCClient master = new XMLRPCClient(slave.getMasterURI());
			Object result = master.call("getTopicTypes", new XMLRPCArray(new Object[] { slave.getCallerId() }));
			XMLRPCArray resultList = (XMLRPCArray) result;
			if ((Integer) resultList.get(0) != 1) {
				return null;
			} else {
				return (ArrayList<ArrayList<String>>) resultList.get(2);
			}
		} catch (IOException | XMLRPCException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ArrayList<ArrayList<ArrayList<Object>>> getSystemState() {
		try {
			XMLRPCClient master = new XMLRPCClient(slave.getMasterURI());
			Object result = master.call("getSystemState", new XMLRPCArray(new Object[] { slave.getCallerId() }));
			XMLRPCArray resultList = (XMLRPCArray) result;
			if ((Integer) resultList.get(0) != 1) {
				return null;
			} else {
				return (ArrayList<ArrayList<ArrayList<Object>>>) resultList.get(2);
			}
		} catch (IOException | XMLRPCException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void memberCleanup() {
		HashSet<String> nodeSet = new HashSet<>();

		ArrayList<ArrayList<ArrayList<Object>>> state = getSystemState();
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

		HashSet<String> toKill = new HashSet<>();
		for (String node : nodeSet) {
			try {
				RosSlaveClient slaveClient = new RosSlaveClient(this, slave, node);
				slaveClient.getPid();
			} catch (Exception e) {
				toKill.add(node);
			}
		}

		for (ArrayList<Object> data : publishers) {
			ArrayList<String> nodes = (ArrayList<String>) data.get(1);
			for (String n : nodes) {
				if (toKill.contains(n)) {
					String nodeApi = lookupNode(n);
					unregisterPublisher(n, nodeApi, (String) data.get(0));
				}
			}
		}
		for (ArrayList<Object> data : subscribers) {
			ArrayList<String> nodes = (ArrayList<String>) data.get(1);
			for (String n : nodes) {
				if (toKill.contains(n)) {
					String nodeApi = lookupNode(n);
					unregisterSubscriber(n, nodeApi, (String) data.get(0));
				}
			}
		}
	}

	private void unregisterPublisher(String node, String nodeApi, String topic) {
//      Do.later(new Runnable() {
//          @Override
//          public void run() {
//          }
//      });
		try {
			XMLRPCClient master = new XMLRPCClient(slave.getMasterURI());
			Object result = master.call("unregisterPublisher",
					new XMLRPCArray(new Object[] { node, topic, nodeApi }));
			XMLRPCArray resultList = (XMLRPCArray) result;
			if ((Integer) resultList.get(0) != 1)
				LOG.info("Unregister publisher " + nodeApi + " failed on server for topic " + topic);

			// TODO better handle a failed unregister

		} catch (IOException | XMLRPCException e) {
			// TODO better handle this error
			LOG.warn("Exception caught while unregistering as publisher", e);
		}
	}

	private void unregisterSubscriber(String node, String nodeApi, String topic) {
//      Do.later(new Runnable() {
//          @Override
//          public void run() {
//          }
//      });
		try {
			XMLRPCClient master = new XMLRPCClient(slave.getMasterURI());
			Object result = master.call("unregisterSubscriber",
					new XMLRPCArray(new Object[] { node, topic, nodeApi }));
			XMLRPCArray resultList = (XMLRPCArray) result;
			if ((Integer) resultList.get(0) != 1)
				LOG.info("Unregister subscriber " + nodeApi + " failed on server for topic " + topic);
		} catch (IOException | XMLRPCException e) {
			LOG.warn("Exception caught while unregistering as subscriber", e);
		}
	}

}
