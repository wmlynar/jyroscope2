package com.jyroscope.ros;

import com.jyroscope.*;
import com.jyroscope.server.xmlrpc.*;


public class XMLRPCSlave {
    
    private static final String NAMESPACE = "/";
    private static final Integer SUCCESS = 1;
    private static final Integer FAILURE = 0;
    private static final Integer ERROR = -1;
    
    final private RosSlave slave;
    
    public XMLRPCSlave(RosSlave slave) {
        this.slave = slave;
    }
    
    private static XMLRPCArray pair(Object itema, Object itemb) {
        XMLRPCArray result = new XMLRPCArray();
        result.add(itema);
        result.add(itemb);
        return result;
    }
    
    private static XMLRPCArray result(Integer code, String statusMessage) {
        return pair(code, statusMessage);
    }
    
    private static XMLRPCArray result(Integer code, String statusMessage, Object value1) {
        XMLRPCArray result = result(code, statusMessage);
        result.add(value1);
        return result;
    }
    
    //getBusStats(caller_id)
    //  Returns (int, str, [XMLRPCLegalValue*])
    //  (code, statusMessage, stats)
    public Object getBusStats(String caller_id) {
        // TODO implement this
        throw new UnsupportedOperationException();
    }
    
    //getBusInfo(caller_id)
    //  Returns (int, str, [XMLRPCLegalValue*])
    //  (code, statusMessage, busInfo)
    public Object getBusInfo(String caller_id) {
        // TODO implement this
        throw new UnsupportedOperationException();
    }
    
    //getMasterUri(caller_id)
    //  Returns (int, str, str)
    //  (code, statusMessage, masterURI)
    public Object getMasterUri(String caller_id) {
        String master_uri = slave.getMasterURI().toASCIIString();
        return result(1, master_uri, master_uri);
    }
    
    //shutdown(caller_id, msg)
    //  Returns (int, str, int)
    //  (code, statusMessage, ignore)
    public Object shutdown(String caller_id, String msg) {
        slave.shutdown(msg);
        return result(1, "shutdown", 0);
    }
    
    //getPid(caller_id)
    //  Returns (int, str, int)
    //  (code, statusMessage, serverProcessPID) 
    public Object getPid(String caller_id) {
        return result(SUCCESS, "Process ID", -1);
    }
    
    //getSubscriptions(caller_id)
    //  Returns (int, str, [ [str, str] ])
    //  (code, statusMessage, topicList)
    public Object getSubscriptions(String caller_id) {
        // TODO implement this
        throw new UnsupportedOperationException();
    }
    
    //getPublications(caller_id)
    //  Returns (int, str, [ [str, str] ])
    //  (code, statusMessage, topicList)
    public Object getPublications(String caller_id) {
        // TODO implement this
        throw new UnsupportedOperationException();
    }
    
    //paramUpdate(caller_id, parameter_key, parameter_value)
    //  Returns (int, str, int)
    //  (code, statusMessage, ignore) 
    public Object paramUpdate(String caller_id, String parameter_key, Object parameter_value) {
        if (slave.parameterUpdate(parameter_key, parameter_value))
            return result(SUCCESS, "parameter updated", 0);
        else
            return result(ERROR, "not subscribed", 0);
    }
    
    //publisherUpdate(caller_id, topic, publishers)
    //  Returns (int, str, int)
    //  (code, statusMessage, ignore) 
    public Object publisherUpdate(String caller_id, String topic, XMLRPCArray publishers) throws SystemException {
        try {
            RosTopic rostopic = slave.getOrCreateTopic(NAMESPACE, caller_id, topic);
            rostopic.publisherUpdate(publishers);
            return result(SUCCESS, "publishers updated", 0);
        } catch (SystemException se) {
            throw new RuntimeException(se);
        }
    }
    
    //requestTopic(caller_id, topic, protocols)
    //  Returns (int, str, [str, !XMLRPCLegalValue*] )
    //  (code, statusMessage, protocolParams)
    public Object requestTopic(String caller_id, String topic, XMLRPCArray protocols) {
        try {
            RosTopic rostopic = slave.getOrCreateTopic(NAMESPACE, caller_id, topic);
            RosTransport transport = slave.requestTopic(caller_id, rostopic, protocols);
            return result(SUCCESS, "ready on " + transport.toConnectionString(), transport.getConnectionInformation());
        } catch (CompatibilityException ce) {
            return result(ERROR, ce.getMessage(), new XMLRPCArray());
        } catch (SystemException se) {
            throw new RuntimeException(se);
        }
    }

}
