package com.jyroscope.ros.parameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.github.jy2.ParameterClient;
import com.github.jy2.ParameterListener;
import com.jyroscope.ros.RosSlave;
import com.jyroscope.server.xmlrpc.XMLRPCArray;
import com.jyroscope.server.xmlrpc.XMLRPCClient;
import com.jyroscope.server.xmlrpc.XMLRPCException;

public class RosParameterClient implements ParameterClient {

	private final RosSlave slave;
	private HashMap<String, ArrayList<ParameterListener>> consumers = new HashMap<>();
	private boolean isSubscribed = false;

	public RosParameterClient(RosSlave slave) {
		this.slave = slave;
	}

	@Override
	public boolean setParameter(String key, Object value) throws IOException {
		try {
			XMLRPCClient master = new XMLRPCClient(slave.getMasterURI());
			Object result = master.call("setParam", new XMLRPCArray(new Object[] { slave.getCallerId(), key, value }));
			XMLRPCArray resultList = (XMLRPCArray) result;
			return (Integer) resultList.get(0) == 1;
		} catch (IOException | XMLRPCException e) {
			throw new IOException(e);
		}

	}

	@Override
	public Object getParameter(String key) throws IOException {
		try {
			XMLRPCClient master = new XMLRPCClient(slave.getMasterURI());
			Object result = master.call("getParam", new XMLRPCArray(new Object[] { slave.getCallerId(), key }));
			XMLRPCArray resultList = (XMLRPCArray) result;
			if ((Integer) resultList.get(0) != 1) {
				return null;
			} else {
				return resultList.get(2);
			}
		} catch (IOException | XMLRPCException e) {
			throw new IOException(e);
		}
	}

	@Override
	public boolean deleteParameter(String key) throws IOException {
		try {
			XMLRPCClient master = new XMLRPCClient(slave.getMasterURI());
			Object result = master.call("deleteParam", new XMLRPCArray(new Object[] { slave.getCallerId(), key }));
			XMLRPCArray resultList = (XMLRPCArray) result;
			return (Integer) resultList.get(0) == 1;
		} catch (IOException | XMLRPCException e) {
			throw new IOException(e);
		}

	}

	@Override
	public boolean hasParameter(String key) throws IOException {
		try {
			XMLRPCClient master = new XMLRPCClient(slave.getMasterURI());
			Object result = master.call("hasParam", new XMLRPCArray(new Object[] { slave.getCallerId(), key }));
			XMLRPCArray resultList = (XMLRPCArray) result;
			if ((Integer) resultList.get(0) != 1) {
				return false;
			} else {
				return (boolean) resultList.get(2);
			}
		} catch (IOException | XMLRPCException e) {
			throw new IOException(e);
		}
	}

	@Override
	public ArrayList<String> getParameterNames() throws IOException {
		try {
			XMLRPCClient master = new XMLRPCClient(slave.getMasterURI());
			Object result = master.call("getParamNames", new XMLRPCArray(new Object[] { slave.getCallerId() }));
			XMLRPCArray resultList = (XMLRPCArray) result;
			if ((Integer) resultList.get(0) != 1) {
				return null;
			} else {
				return (ArrayList<String>) resultList.get(2);
			}
		} catch (IOException | XMLRPCException e) {
			throw new IOException(e);
		}
	}

	@Override
	public synchronized Object addParameterListener(String key, ParameterListener consumer) throws IOException {
		if (!key.endsWith("/")) {
			key = key + "/";
		}
		ArrayList<ParameterListener> list = consumers.get(key);
		if (list == null) {
			list = new ArrayList<>();
			consumers.put(key, list);
			subscribeParameter(key);
		}
		list.add(consumer);
		return new ParameterId(key, consumer);
	}

	@Override
	public synchronized boolean removeParameterListener(Object id) throws IOException {
		ParameterId pid = (ParameterId) id;
		ArrayList<ParameterListener> list = consumers.get(pid.key);
		if (list == null) {
			return false;
		}
		boolean removed = list.remove(pid.consumer);
		if (removed && list.isEmpty()) {
			unsubscribeParameter(pid.key);
		}
		return removed;
	}

	public synchronized boolean handleParameterUpdate(String key, Object value) {
		for (Entry<String, ArrayList<ParameterListener>> c : consumers.entrySet()) {
			if (!key.startsWith(c.getKey())) {
				continue;
			}
			ArrayList<ParameterListener> list = c.getValue();
			if (list != null) {
				for (ParameterListener consumer : list) {
					consumer.onParameterUpdated(key, value);
				}
			}
		}
		return true;
	}

	public void shutdown() {
		if (isSubscribed) {
			try {
				unsubscribeParameter("/");
			} catch (IOException e) {
				// ignore
			}
		}
	}

	private Object subscribeParameter(String key) throws IOException {
		try {
			XMLRPCClient master = new XMLRPCClient(slave.getMasterURI());
			Object result = master.call("subscribeParam",
					new XMLRPCArray(new Object[] { slave.getCallerId(), slave.getSlaveURI().toASCIIString(), key }));
			XMLRPCArray resultList = (XMLRPCArray) result;
			if ((Integer) resultList.get(0) != 1) {
				return null;
			} else {
				isSubscribed = true;
				return resultList.get(2);
			}
		} catch (IOException | XMLRPCException e) {
			throw new IOException(e);
		}
	}

	private int unsubscribeParameter(String key) throws IOException {
		try {
			XMLRPCClient master = new XMLRPCClient(slave.getMasterURI());
			Object result = master.call("unsubscribeParam",
					new XMLRPCArray(new Object[] { slave.getCallerId(), slave.getSlaveURI().toASCIIString(), key }));
			XMLRPCArray resultList = (XMLRPCArray) result;
			if ((Integer) resultList.get(0) != 1) {
				return 0;
			} else {
				return (int) resultList.get(2);
			}
		} catch (IOException | XMLRPCException e) {
			throw new IOException(e);
		}
	}
}
