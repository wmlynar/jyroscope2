package com.jyroscope.ros.types;

public interface RosType {
    
    public String getName();
    public String getHash();
    public boolean isPrimitive();
    public int getSize();
    public int getMinimumSize();

}
