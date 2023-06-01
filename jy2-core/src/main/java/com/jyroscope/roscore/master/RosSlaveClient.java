package com.jyroscope.roscore.master;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.github.jy2.SlaveClient;
import com.jyroscope.roscore.RosSlave;
import com.jyroscope.server.xmlrpc.XMLRPCArray;
import com.jyroscope.server.xmlrpc.XMLRPCClient;
import com.jyroscope.server.xmlrpc.XMLRPCException;

public class RosSlaveClient implements SlaveClient {

	private final RosMasterClient masterClient;
	private final RosSlave slave;
	private final XMLRPCClient slaveClient;

	public RosSlaveClient(RosMasterClient masterClient, RosSlave slave, String name) {
		this.masterClient = masterClient;
		this.slave = slave;
		String address = masterClient.lookupNode(name);
		if (address == null) {
			throw new RuntimeException("Unable to find member " + name);
		}
		try {
			slaveClient = new XMLRPCClient(new URI(address));
		} catch (URISyntaxException e) {
			throw new RuntimeException("Problem with creating XML RPC client", e);
		}
	}

	@Override
	public int getPid() {
		try {
			Object result = slaveClient.call("getPid", new XMLRPCArray(new Object[] { slave.getCallerId() }));
			XMLRPCArray resultList = (XMLRPCArray) result;
			if ((int) resultList.get(0) != 1) {
				return -1;
			}
			return (int) resultList.get(2);
		} catch (IOException | XMLRPCException e) {
			throw new RuntimeException(e);
		}

	}
}
