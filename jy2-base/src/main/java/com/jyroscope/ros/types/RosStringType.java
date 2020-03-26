package com.jyroscope.ros.types;

import com.jyroscope.*;
import java.nio.charset.*;

public enum RosStringType implements RosType {
    
    INSTANCE;
    
    public static final Charset STRING_CHARSET = Encoding.CHARSET;
    
    @Override
    public String getName() {
        return "string";
    }
    
    @Override
    public int getSize() {
        return -1;
    }
    
    @Override
    public int getMinimumSize() {
        return 4;
    }
    
    @Override
    public String getHash() {
        return getName();
    }
    
    @Override
    public boolean isPrimitive() {
        return true;
    }
    
    @Override
    public String toString() {
        return "rostype{string}";
    }
        
}
