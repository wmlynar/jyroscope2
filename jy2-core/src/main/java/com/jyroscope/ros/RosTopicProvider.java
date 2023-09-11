package com.jyroscope.ros;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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

	private ScheduledExecutorService checkerScheduler;
    
    public RosTopicProvider(String prefix, String uri, String localhost, String callerId) {
        this.prefix = prefix + ":";
        URI masterUri = URI.create(uri);
		slave = new RosSlave(masterUri, localhost, callerId);
		parameterClient = new RosParameterClient(slave);
		slave.setParameterClient(parameterClient);
		masterClient = new RosMasterClient(slave);
		startPeriodicDeadPublisherChecker();
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
		stopPeriodicDeadPublisherChecker();
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
	
	private void startPeriodicDeadPublisherChecker() {
		int period = -1;
		try {
			String param = parameterClient.getParameter("/dead_publisher_checker_period_min");
			period = Integer.parseInt(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (period < 1) {
			return;
		}
		checkerScheduler = Executors.newScheduledThreadPool(1);
		checkerScheduler.scheduleAtFixedRate(() -> {
			HashSet<String> nodeSet = new HashSet<>();
			ArrayList<ArrayList<ArrayList<Object>>> state = masterClient.getSystemState();
			ArrayList<ArrayList<Object>> subscribers = state.get(1);
			for (ArrayList<Object> data : subscribers) {
				ArrayList<String> nodes = (ArrayList<String>) data.get(1);
				nodeSet.addAll(nodes);
			}
			closeDeadPublisherConnections(nodeSet);
		}, period, period, TimeUnit.MINUTES);
	}
	
	private void stopPeriodicDeadPublisherChecker() {
		if (checkerScheduler != null) {
			checkerScheduler.shutdown();
		}
	}
	
	private void closeDeadPublisherConnections(HashSet<String> aliveNodes) {
		slave.closeDeadPublisherConnections(aliveNodes);
	}
}
