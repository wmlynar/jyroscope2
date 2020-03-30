package com.jyroscope.ros;

import java.net.URI;
import java.net.URISyntaxException;

import com.github.jy2.MasterClient;
import com.github.jy2.SlaveClient;
import com.jyroscope.SystemException;
import com.jyroscope.local.TopicProvider;
import com.jyroscope.ros.master.RosMasterClient;
import com.jyroscope.ros.master.RosSlaveClient;
import com.jyroscope.ros.parameters.RosParameterClient;
import com.jyroscope.server.xmlrpc.XMLRPCClient;

public class RosTopicProvider implements TopicProvider {

    private final String prefix;
    private final RosSlave slave;

	private final RosParameterClient parameterClient;
	private final RosMasterClient masterClient;
    
    public RosTopicProvider(String prefix, String uri, String localhost, String callerId) {
        this.prefix = prefix + ":";
        URI masterUri = URI.create(uri);
		slave = new RosSlave(masterUri, localhost, callerId);
		parameterClient = new RosParameterClient(slave);
		slave.setParameterClient(parameterClient);
		masterClient = new RosMasterClient(slave);
    }
    
    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public RosTopic getTopic(String name) {
        try {
            return slave.getTopics().parse(name).get();
        } catch (SystemException se) {
            // This should never occur
            throw new RuntimeException(se);
        }
    }

	@Override
	public void shutdown() {
		parameterClient.shutdown();
		for (RosTopic t : slave.getTopics().payloads()) {
			t.shutdown();
		}
	}

	@Override
	public RosParameterClient getParameterClient() {
		return parameterClient;
	}

	@Override
	public MasterClient getMasterClient() {
		return masterClient;
	}

	@Override
	public SlaveClient getSlaveClient(String name) {
		return new RosSlaveClient(masterClient, slave, name);
	}
}
