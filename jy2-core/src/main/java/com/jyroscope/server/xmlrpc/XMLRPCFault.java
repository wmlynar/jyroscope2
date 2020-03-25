package com.jyroscope.server.xmlrpc;

public class XMLRPCFault {
    
    private int code;
    private String message;
    
    public XMLRPCFault(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String toString() {
        return "[" + code + "] " + message;
    }
}