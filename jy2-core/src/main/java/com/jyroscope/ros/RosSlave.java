package com.jyroscope.ros;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import com.jyroscope.CompatibilityException;
import com.jyroscope.Name;
import com.jyroscope.SystemException;
import com.jyroscope.ros.parameters.RosParameterClient;
import com.jyroscope.ros.tcpros.TCPROSServer;
import com.jyroscope.server.http.SimpleHTTPServer;
import com.jyroscope.server.xmlrpc.ReflectedAPI;
import com.jyroscope.server.xmlrpc.XMLRPCArray;
import com.jyroscope.server.xmlrpc.XMLRPCService;

public class RosSlave {

	private static final Logger LOG = Logger.getLogger(RosSlave.class.getCanonicalName());
	
    private final Name<RosTopic> topics;
    private Object topicsMutex = new Object();
    private final URI masterURI;
    private final URI slaveURI;
    private final String callerId;
    private final TCPROSServer tcpros;
	private RosParameterClient parameterClient;
    
	public RosSlave(URI masterURI, String localhostname, String callerId) {
        this.masterURI = masterURI;
        this.callerId = callerId;
        topics = new Name<>(name -> new RosTopic(name, this));
        tcpros = new TCPROSServer(this, localhostname);
        
        XMLRPCSlave rpc = new XMLRPCSlave(this);
        SimpleHTTPServer server = new SimpleHTTPServer(localhostname, new XMLRPCService(new ReflectedAPI(rpc), true), false, "XmlRpcServer");
        slaveURI = server.getURI();
        
        // TODO add shutdown hooks
    }
    
    public URI getSlaveURI() {
        return slaveURI;
    }
    
    public String getCallerId() {
        return callerId;
    }
    
    public boolean parameterUpdate(String key, Object value) {
		parameterClient.handleParameterUpdate(key, value);
		return true;
    }
    
    public URI getMasterURI() {
        return masterURI;
    }
    
    public void shutdown(String msg) {
        LOG.info("Shutdown Request: " + msg);
        tcpros.shutdown();
        // TODO propagate this to the rest of the system
    }
    
	public RosTopic getOrCreateTopic(String name) throws SystemException {
		synchronized (topicsMutex) {
			return topics.parse(name).get();
		}
	}
    
    public RosTopic getOrCreateTopic(String namespace, String node, String topic) {
		synchronized (topicsMutex) {
			Name<RosTopic> name = topics.parse(namespace).parse(node, topic);
			return (RosTopic) name.get();
		}
    }
    
    public RosTopic findTopic(String namespace, String caller_id, String topicName) throws SystemException {
		synchronized (topicsMutex) {
			Name<RosTopic> topic = topics.parse(namespace).parse(caller_id, topicName, false);
			if (topic == null)
				return null;
			else
				return topic.get();
		}
    }
    
    public RosTransport requestTopic(String caller_id, RosTopic rostopic, XMLRPCArray protocols) throws CompatibilityException, SystemException {
        // Currently only TCPROS is supported
        checkTCPROS: {
            for (Object val : protocols) {
                if (val instanceof XMLRPCArray) {
                    XMLRPCArray array = (XMLRPCArray)val;
                    if ("TCPROS".equals(array.get(0)) && array.size() == 1)
                        break checkTCPROS;
                }
            }
            // Does not request TCPROS
            throw new CompatibilityException("Only TCPROS transports are accepted as slave requests"); 
        }
        // We just reuse the same TCP port
        return tcpros;
    }

	public void setParameterClient(RosParameterClient parameterServer) {
		this.parameterClient = parameterServer;
	}
	
	@Override
	public String toString() {
		return "RosSlave [masterURI=" + masterURI + ", slaveURI=" + slaveURI + ", callerId=" + callerId + ", tcpros="
				+ tcpros + "]";
	}

	public void shutdownTopics(ExecutorService service) {
		for (RosTopic t : topics.payloads()) {
			service.execute(() -> t.shutdown());
		}
	}
}
