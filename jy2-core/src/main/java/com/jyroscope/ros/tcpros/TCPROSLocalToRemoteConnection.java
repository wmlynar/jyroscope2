package com.jyroscope.ros.tcpros;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.jy2.di.LogSeldom;
import com.github.jy2.log.Jy2DiLog;
import com.github.jy2.mapper.RosTypeConverters;
import com.jyroscope.Link;
import com.jyroscope.SystemException;
import com.jyroscope.ros.RosMessage;
import com.jyroscope.ros.RosTopic;
import com.jyroscope.ros.RosTopicConnector;
import com.jyroscope.types.ConversionException;

public class TCPROSLocalToRemoteConnection implements Link<RosMessage> {

	private static final Logger LOG = Logger.getLogger(TCPROSLocalToRemoteConnection.class.getCanonicalName());
	
    private TCPROSServer server;
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    
    private RosTopic topic;
    
    private boolean closed;
    private ArrayDeque<RosMessage> messages;
    
    public TCPROSLocalToRemoteConnection(TCPROSServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
        this.messages = new ArrayDeque<>();
    }
    
    public void open() {
        try {
            is = socket.getInputStream();
            os = new BufferedOutputStream(socket.getOutputStream());
            
            TCPROSHeader header = new TCPROSHeader();
            RosMessage buffer = new RosMessage();
            if (buffer.readIn(is)) {
                header.parse(buffer);
            
                // For some reason, the rostopic echo client reconnects with empty headers during a disconnect
                if (header.count() != 0) {

                    String caller = header.getHeader("callerid");
                    String topicName = header.getHeader("topic");
                    String md5sum = header.getHeader("md5sum");
                    String typeName = header.getHeader("type");

					// final RosMessageType type = RosTypes.getMessageType(typeName);
					String md5 = RosTypeConverters.getMd5(typeName);

                    String error;
					if (md5 == null)
                        error = "Unrecognized type name " + typeName;
					else if (!md5.equals(md5sum))
                        error = "MD5 checksums do not match for type " + typeName;
                    else
                    	error = null;
                    
					// woj: handle case when type is "*", for example: rostopic hz /topic
					if(error == null || "*".equals(typeName))
                    {
                        topic = server.findTopic(caller, topicName);

                        // woj: handle case when requested topic type is "*"
						if ("*".equals(typeName) && topic != null) {
	                    	try {
								typeName = topic.getRosType();
								md5 = RosTypeConverters.getMd5(typeName);
							} catch (ConversionException e) {
								LOG.log(Level.SEVERE, "Unknown type " + typeName + " for topic " + topicName, e);
								typeName = null;
							}
						}

                        if (topic == null)
                            error = "Unrecognized topic " + topicName;
                        else if (typeName == null)
                            error = "Unknown type for topic " + topicName;
                        else {

                            TCPROSHeader reply = new TCPROSHeader();
							reply.putHeader("md5sum", md5);
                            reply.putHeader("type", typeName);
							reply.putHeader("latching", topic.isLatched() ? "1" : "0");
							String javaType = topic.getJavaTypeIfExists();
							if (javaType != null) {
								reply.putHeader("java_type", javaType);
							}
							reply.render(buffer);
                            buffer.writeOut(os);
                            os.flush();
                            topic.addRemoteSubscriber(this);
                            // Successful connection established!
                            // Process messages
                            messageLoop();
                            return;
                        }
                    }

                    // at this point, error != null
                    // TODO verify this -- the specification/documentation isn't very clear about what to do in the event of an error
                    TCPROSHeader reply = new TCPROSHeader();
                    reply.putHeader("error", error);
                    reply.render(buffer);
                    buffer.writeOut(os);
                    LOG.info("Replying with error to TCPROS socket: " + error);
                    socket.close();

                } else {
                    // The client has sent empty headers. Just disconnect.
                    socket.close();
                }
            } else {
                // The client has not sent full headers. Just disconnect.
                socket.close();
            }
        } catch (SystemException se) {
        	LOG.log(Level.SEVERE, "Could not connect to publisher while communicating with TCPROS client", se);
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Error while communicating with TCPROS client, disconnecting", e);
			try {
				topic.removeRemoteSubscriber(this);
			} catch (Exception e2) {
				LOG.log(Level.WARNING, "Problem with removing remote subscriber", e2);
			}
            try {
                socket.close();
            } catch (IOException ioe2) {
            	LOG.log(Level.SEVERE, "Error while closing socket", ioe2);
            }
        }
    }
    
    private void messageLoop() {
        try {
            while (true) {
                RosMessage message;
                boolean hasNext;
                synchronized (this) {
                    message = messages.poll();
                    hasNext = messages.isEmpty();
                    if (message == null && !closed) {
                        try {
                            this.wait();
                        } catch (InterruptedException ex) {
                            // do nothing
                        }
                    }
                }
                if (message != null) {
                    message.writeOut(os);
                    if (hasNext)
                        os.flush();
                } else if (closed)
                    break;
            }
        } catch (IOException ioe) {
//            Log.exception(this, ioe, "Error while communicating with TCPROS client");
        	LOG.log(Level.WARNING, "Error while communicating with TCPROS client, disconnecting", ioe);
        }
        
        topic.removeRemoteSubscriber(this);
        
        try {
            socket.close();
        } catch (IOException ioe) {
        	LOG.log(Level.SEVERE, "Error while closing socket", ioe);
        }
    }

    @Override
    public Class<? extends RosMessage> getType() {
        return RosMessage.class;
    }

    @Override
    public synchronized void handle(RosMessage message) {
        messages.offer(message);
        this.notify();
    }

    public synchronized void close() {
        // The queue is drained before the close takes effect
        closed = true;
        this.notify();
    }

	@Override
	public void setRemoteAttributes(boolean isLatched, String remoteRosType, String remoteJavaType) {
		throw new UnsupportedOperationException("Reserved for class Receive in LinkManager");
	}

}
