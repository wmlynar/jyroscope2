package com.jyroscope;

@FunctionalInterface
public interface PayloadFactory<P> {
    
    public P newInstance(Name<P> name) throws SystemException;

}
