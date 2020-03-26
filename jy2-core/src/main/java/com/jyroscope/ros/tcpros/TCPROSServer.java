package com.jyroscope.ros.tcpros;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;

import com.jyroscope.Name;
import com.jyroscope.SystemException;
import com.jyroscope.ros.RosNode;
import com.jyroscope.ros.RosTopic;
import com.jyroscope.ros.RosTransport;
import com.jyroscope.server.xmlrpc.XMLRPCArray;

public class TCPROSServer implements RosTransport {
    
    private static final String NAMESPACE = "/";
    private static final int SHUTDOWN_TIME = 5000;
    
    private String hostname;
    private int port;
    private volatile boolean started;
    private volatile boolean stopped;
    
    private Name<RosTopic> root;
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
    
    public TCPROSServer(Name<RosTopic> root, String hostname) {
        this.root = root;
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
        Name<RosTopic> topic = root.parse(NAMESPACE).parse(caller_id, topicName, false);
        if (topic == null)
            return null;
        else
            return topic.get();
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
									}, "TCPROSConnection").start();
                                } catch (SocketTimeoutException ste) {
                                    // do nothing -- timeout is to check that the server hasn't been stopped
                                }
                            }
                            started = false;
                            stopped = false;
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                        	System.err.println("Unable to listen on interface " + hostname);
                            e.printStackTrace();
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

}
