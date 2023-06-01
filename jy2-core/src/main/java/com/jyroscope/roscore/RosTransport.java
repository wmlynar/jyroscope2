package com.jyroscope.roscore;

import com.jyroscope.server.xmlrpc.*;

public interface RosTransport {
    
    public XMLRPCArray getConnectionInformation();
    public String toConnectionString();

}
