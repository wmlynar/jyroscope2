package com.jyroscope.ros;

import com.jyroscope.server.xmlrpc.*;

public interface RosTransport {
    
    public XMLRPCArray getConnectionInformation();
    public String toConnectionString();

}
