package com.jyroscope.server.xmlrpc;

public interface Method {

    public Object process(Object message) throws Exception;
    
}
