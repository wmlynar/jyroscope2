package com.jyroscope.server.xmlrpc;

import org.w3c.dom.*;

public class XMLParseException extends Exception {
    
    private Element origin;
    
    public XMLParseException(String message, Element origin) {
        super(message);
        this.origin = origin;
    }

}
