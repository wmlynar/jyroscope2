package com.jyroscope.ros.master;

import java.io.IOException;
import java.util.ArrayList;

import com.github.jy2.MasterClient;
import com.jyroscope.ros.RosSlave;
import com.jyroscope.server.xmlrpc.XMLRPCArray;
import com.jyroscope.server.xmlrpc.XMLRPCClient;
import com.jyroscope.server.xmlrpc.XMLRPCException;

public class RosMasterClient implements MasterClient {

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
				return (ArrayList<ArrayList<ArrayList<Object>>>)resultList.get(2);
			}
		} catch (IOException | XMLRPCException e) {
			throw new RuntimeException(e);
		}
	}

}
