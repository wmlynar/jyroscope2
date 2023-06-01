package com.jyroscope.roscore.tcpros;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jyroscope.SystemException;
import com.jyroscope.roscore.RosNode;
import com.jyroscope.roscore.RosSlave;
import com.jyroscope.roscore.RosTopic;
import com.jyroscope.roscore.RosTransport;
import com.jyroscope.server.xmlrpc.XMLRPCArray;

public class TCPROSServer implements RosTransport {
    
	private static final Logger LOG = Logger.getLogger(TCPROSServer.class.getCanonicalName());
	
    private static final String NAMESPACE = "/";
    private static final int SHUTDOWN_TIME = 5000;
    
    private String hostname;
    private int port;
    private volatile boolean started;
    private volatile boolean stopped;
    
    private RosSlave slave;
    private ServerSocket serverSocket;
    private HashMap<Registration, RosNode> registrations;
    
    private class Registration {
        private String caller_id;
        private RosTopic topic;
        
        private Registration(String caller_id, RosTopic topic) {
            this.caller_id = caller_id;
            this.topic = topic;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (this.getClass() != o.getClass())
                return false;
            Registration other = (Registration)o;
            return this.caller_id.equals(other.caller_id) && this.topic.equals(other.topic);
        }
        
        public int hashCode() {
            return caller_id.hashCode() * 7 + topic.hashCode();
        }
    }
    
    public TCPROSServer(RosSlave slave, String hostname) {
        this.slave = slave;
        this.hostname = hostname;
        this.started = false;
        this.registrations = new HashMap<TCPROSServer.Registration, RosNode>();
        
        start();
    }
    
    public String getHostName() {
        return hostname;
    }
    
    public int getPort() {
        return port;
    }
    
//    public void registerPublisher(String caller_id, RosTopic topic, RosNode publisher) {
//        Registration registration = new Registration(caller_id, topic);
//        registrations.put(registration, publisher);
//    }
    
    public RosTopic findTopic(String caller_id, String topicName) throws SystemException {
    	return slave.findTopic(NAMESPACE, caller_id, topicName);
    }
    
//    public RosNode getRegisteredPublisher(String caller_id, RosTopic topic) {
//        Registration registration = new Registration(caller_id, topic);
//        return registrations.get(registration);
//    }
    
    private void start() {
        synchronized (this) {
            if (!started) {
                started = true;
                stopped = false;
//                Do.service(new Runnable() {
				new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            serverSocket = new ServerSocket(0, 0, InetAddress.getByName(hostname));
                            serverSocket.setSoTimeout(SHUTDOWN_TIME);
                            port = serverSocket.getLocalPort();
							Thread.currentThread().setName("TCPROSServer-" + hostname + ":" + port);
                            while (!stopped) {
                                try {
                                    final Socket socket = serverSocket.accept();
									socket.setTcpNoDelay(true);
									// turning this on might cause false peer death detection on temporary network outage
									// however javadoc says 2 hours have to pass before keepalive packet is sent
									socket.setKeepAlive(true);
//                                    Do.connection(new Runnable() {
									new Thread(new Runnable() {
                                        public void run() {
                                            TCPROSLocalToRemoteConnection conn = new TCPROSLocalToRemoteConnection(TCPROSServer.this, socket);
                                            conn.open();
                                        }
//                                    });
									}, "Publisher").start();
                                } catch (SocketTimeoutException ste) {
                                    // do nothing -- timeout is to check that the server hasn't been stopped
                                }
                            }
                            started = false;
                            stopped = false;
						} catch (IOException e) {
							LOG.log(Level.SEVERE,
									"TCPROS server unable to listen on interface " + hostname, e);
						}
                    }
//                });
				}, "TCPROSServer").start();
            }
        }
    }
    
    public void shutdown() {
        stopped = false;
    }

    @Override
    public XMLRPCArray getConnectionInformation() {
        return new XMLRPCArray(new Object[] {"TCPROS", hostname, port});
    }
    
    @Override
    public String toConnectionString() {
        return "tcpros:" + hostname + ":" + port;
    }

	@Override
	public String toString() {
		return "TCPROSServer [hostname=" + hostname + ", port=" + port + ", started=" + started + ", stopped=" + stopped
				+ "]";
	}    
}
