package com.jyroscope.ros.types;

public enum RosPrimitiveType implements RosType {
    
    Bool("bool", 1),

    Int8("int8", 1),
    Int16("int16", 2),
    Int32("int32", 4),
    Int64("int64", 8),
    
    UInt8("uint8", 1),
    UInt16("uint16", 2),
    UInt32("uint32", 4),
    UInt64("uint64", 8),
    
    Float32("float32", 4),
    Float64("float64", 8),
    
    Duration("duration", 8),
    Time("time", 8),
    
    Byte("byte", Int8),
    Char("char", UInt8);
    
    private String name;
    private int size;
    private RosPrimitiveType underlying;
    
    private RosPrimitiveType(String name, int size) {
        this.name = name;
        this.size = size;
    }
    
    private RosPrimitiveType(String name, RosPrimitiveType underlying) {
        this.name = name;
        this.underlying = underlying;
        this.size = underlying.size;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public int getMinimumSize() {
        return size;
    }
    
    @Override
    public String getHash() {
        return getName();
    }
    
    public RosPrimitiveType getCanonicalType() {
        if (underlying == null)
            return this;
        else
            return underlying;
    }
    
    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public String toString() {
        return "rostype{" + getName() + "}";
    }
        
}
