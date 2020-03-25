package com.jyroscope.server.xmlrpc;

public class MethodCall {
    
    private String name;
    private XMLRPCArray params;
    
    public MethodCall(String name) {
        this.name = name;
        params = new XMLRPCArray();
    }
    
    public void addParam(Object param) {
        params.add(param);
    }
    
    public String getName() {
        return name;
    }
    
    public XMLRPCArray getParams() {
        return params;
    }
    
    public String toString() {
        return name + params;
    }
}