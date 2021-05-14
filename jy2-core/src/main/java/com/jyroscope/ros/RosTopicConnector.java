package com.jyroscope.ros;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.jy2.mapper.RosTypeConverters;
import com.jyroscope.Link;
import com.jyroscope.SystemException;
import com.jyroscope.ros.tcpros.TCPROSRemoteToLocalConnection;
import com.jyroscope.server.xmlrpc.XMLRPCArray;
import com.jyroscope.server.xmlrpc.XMLRPCClient;
import com.jyroscope.server.xmlrpc.XMLRPCException;
import com.jyroscope.types.ConversionException;

public class RosTopicConnector {
    
	private static final Logger LOG = Logger.getLogger(RosTopicConnector.class.getCanonicalName());
	
    private final RosTopic topic;
    private final URI slaveURI;
    private final RosSlave localSlave;
    private Link<RosMessage> listener;
    private volatile boolean connected = false;
	private boolean remoteIsLatched;
	private String remoteJavaType;
	private String remoteRosType;
	private Semaphore semaphore = new Semaphore(1);
	
	// WOJ: bugfix for disconnecting. we need to be able to close the connection (blocked on read)
	// otherwise "connected" might be set to true on when starting another subscriber
	// which will fail to close it and we might be getting duplicated messages
	private TCPROSRemoteToLocalConnection lastConnection = null;
    
    public RosTopicConnector(RosTopic topic, URI slaveURI, RosSlave localSlave) {
        this.topic = topic;
        this.slaveURI = slaveURI;
        this.localSlave = localSlave;
    }
    
    /*
     * Opens a persistent TCPROS connection to the remote Slave.
     * Throws an exception if the connection fails (meaning that the listener is NOT connected and therefore the internal list of listeners is set to be empty).
     */
    public void connect(Link<RosMessage> listener) {
        this.listener = listener;
        connected = true;
        try {
			this.semaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//        Do.later(new Runnable() {
		new Thread(new Runnable() {
			@Override
			public void run() {
                try {
                    open();
                } catch (Exception e) {
                	LOG.log(Level.SEVERE, "Exception caught", e);
                }
			}
//        });
		}, "Subscriber-" + topic.getName()).start();
    }
    
    private void open() throws SystemException {
		int messageSize;
		TCPROSRemoteToLocalConnection subscriber;
        try {
            XMLRPCClient slave = new XMLRPCClient(slaveURI);
            XMLRPCArray result = (XMLRPCArray)slave.call("requestTopic", new XMLRPCArray(new Object[] {localSlave.getCallerId(), topic.getName().toNameString(), new XMLRPCArray(new Object[] {new XMLRPCArray(new Object[] {"TCPROS"})})}));
    
            if (result.get(0).equals(1)) {
				XMLRPCArray parameters = (XMLRPCArray) result.get(2);
				String host = (String) parameters.get(1);
				Integer port = (Integer) parameters.get(2);

				messageSize = RosTypeConverters.getSize(topic.getRosType());

				subscriber = new TCPROSRemoteToLocalConnection(host, port);
				subscriber.setParameters(localSlave.getCallerId(), topic.getName().toNameString(), topic.getRosType(), topic.getJavaTypeIfExists());

				if (!subscriber.connect())
					throw new SystemException("Unexpected end of stream while connecting to publisher");

				// WOJ: store reference to the subscriber to be able to close it
				storeLastConnection(subscriber);

				this.remoteIsLatched = subscriber.getRemoteIsLatched();
				this.remoteJavaType = subscriber.getRemoteJavaType();
				this.remoteRosType = subscriber.getRemoteRosType();
				// bugfix for race condition in latched messages, in situation of
				// one latched remote publisher and few local subscribers created with pause between them
				// In such case only the first subscriber got the latched message.
				// The fix below informs local broker that message is latched and when new local
				// subscriber joins he gets the latched message that was received form remote connection
				this.listener.setRemoteAttributes(this.remoteIsLatched, this.remoteRosType, this.remoteJavaType);
            } else {
                //throw new SystemException("Could not open TCPROS connection to " + slaveURI + " (" + String.valueOf(result.get(1)) + ")");
            	LOG.warning("Could not open TCPROS connection to " + slaveURI + " (" + String.valueOf(result.get(1)) + ")");
            	return;
            }
        } catch (XMLRPCException | IOException e) {
//            throw new SystemException("Could not open TCPROS connection to " + slaveURI, e);
        	LOG.log(Level.WARNING, "Could not open TCPROS connection to " + slaveURI, e);
        	return;
        } catch (ConversionException ex) {
            throw new SystemException("Could not create TCPROS connection", ex);
		} finally {
			this.semaphore.release();
		}
		handleMessages(subscriber, messageSize);
    }

	private void handleMessages(final TCPROSRemoteToLocalConnection subscriber, int messageSize) {
		try {
			RosMessage buffer = new RosMessage(messageSize);
		    while (connected) {
		        if (subscriber.read(buffer))
		            listener.handle(buffer);
		        else {
		            LOG.info("Publisher closed connection");
		            break;
		        }
		    }
		    
		    try {
		        subscriber.close();
		    } catch (IOException ioe) {
		        LOG.log(Level.SEVERE, "Error while closing connection to topic " + topic, ioe);
		    }
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Exception while reading from connection to topic " + topic, e);
		} catch (Exception e) {
	        // added try-catch for null pointers when missing type converter etc.
			LOG.log(Level.SEVERE, "Exception while reading from connection to topic " + topic, e);
		    try {
		        subscriber.close();
		    } catch (IOException ioe) {
		    	LOG.log(Level.SEVERE, "Error while closing connection to topic " + topic, ioe);
		    }
		}
	}

    void disconnect() {
        // TODO handle this better -- try to unblock the subscriber.read() so that it isn't sitting around on an unsubscribed connection
        // Note that this method should not block
        connected = false;
        closeLastConnection();
    }

	public boolean isRemoteLatched() {
        try {
			this.semaphore.acquire();
			return remoteIsLatched;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			semaphore.release();
		}
        return false;
	}

	public String getRemoteJavaType() {
        try {
			this.semaphore.acquire();
			return remoteJavaType;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			semaphore.release();
		}
        return null;
	}
	
	public String getRemoteRosType() {
        try {
			this.semaphore.acquire();
			return remoteRosType;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			semaphore.release();
		}
        return null;
	}
	
	private void storeLastConnection(TCPROSRemoteToLocalConnection connection) {
		lastConnection = connection;
	}

	private void closeLastConnection() {
		if (lastConnection != null) {
			try {
				lastConnection.close();
			} catch (IOException e) {
				LOG.warning("Could not close connection to remote subscriber");
			}
			lastConnection = null;
		}
	}
}
