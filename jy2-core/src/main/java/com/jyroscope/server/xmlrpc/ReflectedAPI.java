package com.jyroscope.server.xmlrpc;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import com.github.jy2.util.ExceptionUtil;

public class ReflectedAPI implements API {
    
    private HashMap<String, Method> methods;
    
    public ReflectedAPI(Object target) {
        methods = new HashMap<String, Method>();
        add(target);
    }

    public void add(Object target) {
        Class<?> clazz = target.getClass();
        for (java.lang.reflect.Method method : clazz.getMethods()) {
            Method m = makeXMLRPCMethod(target, method);
            methods.put(method.getName(), m);
        }
    }
    
    private Method makeXMLRPCMethod(final Object target, final java.lang.reflect.Method method) {
        return new Method() {
            public Object process(Object message) throws Exception {
                ArrayList<Object> params = (ArrayList<Object>)message;
                try {
					return method.invoke(target, params.toArray());
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					ExceptionUtil.rethrowErrorIfCauseIsError(e);
					Throwable t = ExceptionUtil.getCauseIfInvocationException(e);
					throw new RuntimeException("Exception caught while executing xml rpc method " + method.toGenericString(), t);
				}
            }
        };
    }

    @Override
    public Method getMethod(String name) {
        return methods.get(name);
    }

}
