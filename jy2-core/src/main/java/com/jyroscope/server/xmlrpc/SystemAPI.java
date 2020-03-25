package com.jyroscope.server.xmlrpc;

public class SystemAPI implements API {
    
    private API base;
    private SystemMultiCall smc;
    
    public SystemAPI(API base) {
        this.base = base;
        smc = new SystemMultiCall();
    }
    
    @Override
    public Method getMethod(String name) {
        if (name.equals("system.multicall"))
            return smc;
        else
            return base.getMethod(name);
    }

    public class SystemMultiCall implements Method {
        @Override
        public Object process(Object message) throws Exception {
            XMLRPCArray results = new XMLRPCArray();
            XMLRPCArray params = (XMLRPCArray)message;
            XMLRPCArray calls = (XMLRPCArray)params.get(0);
            for (Object item : calls) {
                XMLRPCStruct struct = (XMLRPCStruct)item;
                String methodName = (String)struct.get("methodName");
                Object methodParams = struct.get("params");
                Object result = base.getMethod(methodName).process(methodParams);
                results.add(result);
            }
            return results;
        }

    }

}