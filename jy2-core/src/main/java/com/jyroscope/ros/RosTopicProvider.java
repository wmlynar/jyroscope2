package com.jyroscope.ros;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.github.jy2.MasterClient;
import com.github.jy2.SlaveClient;
import com.jyroscope.SystemException;
import com.jyroscope.local.TopicProvider;
import com.jyroscope.ros.master.RosMasterClient;
import com.jyroscope.ros.master.RosSlaveClient;
import com.jyroscope.ros.parameters.RosParameterClient;

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
            return slave.getOrCreateTopic(name);
        } catch (SystemException se) {
            // This should never occur
            throw new RuntimeException(se);
        }
    }

	@Override
	public void shutdown(ExecutorService service) {
		service.execute(() -> parameterClient.shutdown());
		slave.shutdownTopics(service);
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
