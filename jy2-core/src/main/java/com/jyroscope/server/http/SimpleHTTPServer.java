package com.jyroscope.server.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import com.jyroscope.Log;

public class SimpleHTTPServer {
    
    private static final int DEFAULT_BACKLOG = 50; // using the same default as in the ServerSocket source code
    
    private int port;
    private String host;
    private volatile boolean running = true;

    public SimpleHTTPServer(String host, HTTPService service, boolean isDaemon) {
        this(host, -1, service, isDaemon);
    }
    
    public SimpleHTTPServer(String host, int port, final HTTPService service, boolean isDaemon) {
        this.host = host;
        try {
            final ServerSocket ss;
            
            if (port == -1) {
                ss = new ServerSocket(0, DEFAULT_BACKLOG, InetAddress.getByName(host));
                this.port = ss.getLocalPort();
            } else {
                ss = new ServerSocket(port, DEFAULT_BACKLOG, InetAddress.getByName(host));
                this.port = port;
            }
            
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (running) {
                            final Socket s = ss.accept();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    processRequest(s, service);
                                    try {
                                        s.close();
                                    } catch (IOException ioe) {
                                        Log.exception(s, ioe, "Could not close connection with client");
                                    }
                                }
                            }).start();
                        }
                    } catch (IOException ioe) {
                    	System.err.println("Unable to listen on interface " + host);
                        ioe.printStackTrace();
                    }
                }
			}, "SimpleHTTPServer");
            if (isDaemon)
                thread.setDaemon(true);
            thread.start();
        } catch (IOException ioe) {
        	System.err.println("Unable to listen on interface " + host);
            ioe.printStackTrace();
        }
    }
    
    public URI getURI() {
        try {
            return new URI("http", null, host, port, null, null, null);
        } catch (URISyntaxException use) {
            // this should never happen
            throw new RuntimeException("Invalid host syntax", use);
        }
    }
    
    private void processRequest(Socket s, HTTPService service) {
        try {
            HTTPAction a = new HTTPAction(s.getRemoteSocketAddress());
            try {
                a.parseIn(s.getInputStream());
                service.process(a);
            } catch (HTTPException he) {
                he.printStackTrace();
                he.writeOut(s.getOutputStream());
                return;
            }
            a.writeOut(s.getOutputStream());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public void stop() {
        running = false;
    }
    
}
