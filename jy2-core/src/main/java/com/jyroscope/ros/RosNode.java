package com.jyroscope.ros;

import java.net.URI;
import java.util.HashMap;

import com.jyroscope.Link;
import com.jyroscope.Name;

/**
 * A RosNode represents a *remote* ROS Node
 */
public class RosNode {
    
    private final Name<RosNode> name;
    private final URI remoteSlaveURI;
    private final RosSlave localSlave;
    
    private HashMap<RosTopic,RosTopicConnector> connectors;
    
    public RosNode(Name<RosNode> name, URI remoteSlaveURI, RosSlave localSlave) {
        this.name = name;
        this.remoteSlaveURI = remoteSlaveURI;
        this.localSlave = localSlave;
        this.connectors = new HashMap<>();
    }
    
    public Name<RosNode> getName() {
        return name;
    }
    
    public URI getSlaveURI() {
        return remoteSlaveURI;
    }
    
    @Override
    public String toString() {
        return name + "{" + remoteSlaveURI + "}";
    }
    
    public String getId() {
        return remoteSlaveURI.toASCIIString();
    }
    
    public void connect(RosTopic topic) {
        Link<RosMessage> remotePublisher = topic.getRemotePublisher();
        RosTopicConnector connector = connectors.get(topic);
        if (connector == null)
            connectors.put(topic, connector = new RosTopicConnector(topic, remoteSlaveURI, localSlave));
        connector.connect(remotePublisher);
    }

    public void disconnect(RosTopic topic) {
        RosTopicConnector connector = connectors.get(topic);
        if (connector != null)
            connector.disconnect();
    }

	public boolean isRemoteLatched(RosTopic topic) {
		RosTopicConnector connector = connectors.get(topic);
		if (connector != null) {
			return connector.isRemoteLatched();
		}
		return false;
	}

	public String getRemoteJavaType(RosTopic topic) {
		RosTopicConnector connector = connectors.get(topic);
		if (connector != null) {
			return connector.getRemoteJavaType();
		}
		return null;
	}

	public String getRemoteRosType(RosTopic topic) {
		RosTopicConnector connector = connectors.get(topic);
		if (connector != null) {
			return connector.getRemoteRosType();
		}
		return null;
	}

}
