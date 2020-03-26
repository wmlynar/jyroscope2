package com.jyroscope.ros.tcpros;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.github.jy2.mapper.RosTypeConverters;
import com.jyroscope.FormatException;
import com.jyroscope.ros.RosMessage;

public class TCPROSRemoteToLocalConnection {
    
    private String host;
    private int port;
    
    private String callerid;
    private String topic;
	private boolean tcpNoDelay = true;
    
    private OutputStream os;
    private InputStream is;
    private Socket socket;
	private String typeName;
	private String javaTypeName;
	private boolean remoteIsLatched;
	private String remoteJavaType;
	private String remoteRosType;

    public TCPROSRemoteToLocalConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
	public void setParameters(String callerid, String topic, String typeName, String javeTypeName) {
        this.callerid = callerid;
        this.topic = topic;
		this.typeName = typeName;
		this.javaTypeName = javeTypeName;
    }
    
    public void setTCPNoDelay() {
        tcpNoDelay = true;
    }
    
    public boolean connect() throws IOException {
        RosMessage buffer = new RosMessage();
        
        socket = new Socket(host, port);
		socket.setTcpNoDelay(tcpNoDelay);
		// turning this on might cause false peer death detection on temporary network outage
		// however javadoc says 2 hours have to pass before keepalive packet is sent
		socket.setKeepAlive(true);
		
        os = socket.getOutputStream();
        
		TCPROSHeader request = new TCPROSHeader();
		request.putHeader("callerid", callerid);
		request.putHeader("topic", topic);
		request.putHeader("md5sum", RosTypeConverters.getMd5(typeName));
		request.putHeader("type", typeName);
		if (tcpNoDelay)
			request.putHeader("tcp_nodelay", "1");
		if (javaTypeName != null) {
			request.putHeader("java_type", javaTypeName);
		}
		request.render(buffer);
		buffer.writeOut(os);
		os.flush();
        
        is = socket.getInputStream();
        
        TCPROSHeader acknowledge = new TCPROSHeader();
        if (buffer.readIn(is)) {
            acknowledge.parse(buffer);
			this.remoteIsLatched = "1".equals(acknowledge.getHeader("latching"));
			this.remoteJavaType = acknowledge.getHeader("java_type");
			this.remoteRosType = acknowledge.getHeader("type");
            return true;
        } else
            return false;
    }
    
    public boolean read(RosMessage buffer) throws IOException, FormatException {
        return buffer.readIn(is);
    }
    
    public void close() throws IOException {
        is.close();
        os.close();
        socket.close();
    }

	public boolean getRemoteIsLatched() {
		return remoteIsLatched;
	}

	public String getRemoteJavaType() {
		return remoteJavaType;
	}

	public String getRemoteRosType() {
		return remoteRosType;
	}

}
