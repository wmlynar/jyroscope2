package com.jyroscope.server.xmlrpc;

import java.util.*;

public class XMLRPCArray extends ArrayList<Object> {

    public XMLRPCArray() {
        // do nothing
    }
    
    public XMLRPCArray(Object[] entries) {
        super(entries.length);
        for (Object entry : entries)
            add(entry);
    }
    
}
