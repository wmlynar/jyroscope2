package com.jyroscope.server.http;

import java.io.*;

public class HTTPException extends Exception {
    
    private static byte[] CRLF = {13, 10};
    
    private int code;
    private String message;
    private Throwable cause;
    private String fullMessage;

    public HTTPException(int code, String message) {
        super("HTTP Error " + code + ": " + message);
        this.code = code;
        this.message = message;
    }
    
    public HTTPException(int code, String message, Throwable cause) {
        super("HTTP Error " + code + ": " + message, cause);
        this.code = code;
        this.message = message;
        this.cause = cause;
    }
    
    public HTTPException(int code, String message, String fullMessage) {
        super("HTTP Error " + code + ": " + message + "(" + fullMessage + ")");
        this.code = code;
        this.message = message;
        this.fullMessage = fullMessage;
    }

    
    public void writeOut(OutputStream os) throws IOException {
        os = new BufferedOutputStream(os);
        os.write(("HTTP/1.0 " + code + " " + message).getBytes());
        os.write(CRLF);
        os.write("Content-Type: text/plain".getBytes());
        os.write(CRLF);
        
        byte[] responseContent;
        if (cause != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            cause.printStackTrace(ps);
            ps.close();
            responseContent = baos.toByteArray();
        } else if (fullMessage != null)
            responseContent = fullMessage.getBytes();
        else
            responseContent = message.getBytes();
        
        os.write(("Content-Length: " + responseContent.length).getBytes());
        os.write(CRLF);
        os.write(CRLF);
        os.write(responseContent);
        
        os.close();
    }
}