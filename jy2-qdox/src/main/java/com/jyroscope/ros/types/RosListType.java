package com.jyroscope.ros.types;

public class RosListType implements RosType {

    private RosType base;
    private int size;
    
    public RosListType(RosType base) {
        this.base = base;
        this.size = -1;
    }
    
    public RosListType(RosType base, int size) {
        this.base = base;
        this.size = size;
    }
    
    @Override
    public String getName() {
        if (size == -1)
            return base.getName() + "[]";
        else
            return base.getName() + "[" + size + "]";
    }
    
    @Override
    public int getSize() {
        if (size == -1)
            return -1;
        
        int baseSize = base.getSize();
        if (baseSize == -1)
            return baseSize;
        
        return baseSize * size;
    }
    
    @Override
    public int getMinimumSize() {
        if (size == -1)
            return 4;
        else
            return size * base.getMinimumSize();
    }
    
    @Override
    public String getHash() {
        if (base.isPrimitive()) {
            return getName();
        } else
            return base.getHash();
    }
    
    public RosType getBaseType() {
        return base;
    }
    
    @Override
    public boolean isPrimitive() {
        return false;
    }
    
    @Override
    public int hashCode() {
        return base.hashCode() + size * 65537;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (this.getClass() != o.getClass())
            return false;
        RosListType other = (RosListType)o;
        return this.base.equals(other.base) && this.size == other.size;
    }
    
    public int getDeclaredLength() {
        return size;
    }
    
//    @Override
//    public boolean hasFixedSerializationSize() {
//        if (size != -1 && base.hasFixedSerializationSize())
//            return true;
//        else
//            return false;
//    }
//    
//    @Override
//    public int getSize(Object value) throws FormatException {
//        int length;
//        if (value instanceof Collection)
//            length = ((Collection<?>)value).size();
//        else
//            length = Array.getLength(value);
//        
//        if (size != -1 && length != size)
//            throw new FormatException("Array does not match declared length");
//        
//        if (size == -1)
//            return 4 + base.getSizeList(value);
//        else
//            return base.getSizeList(value);
//    }
//    
//    @Override
//    public int getSizeList(Object list) {
//        // This error should never occur because we shouldn't be able to parse a message that includes multidimensional arrays
//        throw new UnsupportedOperationException("ROS does not support multidimensional arrays (this error should not occur)");
//    }
//
//    @Override
//    public void serialize(Object value, ByteBuffer ob) throws FormatException {
//        int length;
//        if (value instanceof Collection)
//            length = ((Collection<?>)value).size();
//        else
//            length = Array.getLength(value);
//        
//        if (size == -1)
//            ob.putInt(length);
//        else if (length != size)
//            throw new FormatException("Serialized array does not match declared length");
//        
//        base.serializeList(value, ob);
//    }

//    @Override
//    public Object deserialize(ByteBuffer ib) throws FormatException {
//        int length = size;
//        if (size == -1)
//            length = ib.getInt();
//        return base.deserializeList(ib, length);
//    }
//    
//    @Override
//    public void serializeList(Object list, ByteBuffer ob) throws FormatException {
//        // This error should never occur because we shouldn't be able to parse a message that includes multidimensional arrays
//        throw new UnsupportedOperationException("ROS does not support multidimensional arrays (this error should not occur)");
//    }
//
//    @Override
//    public Object deserializeList(ByteBuffer ib, int count) throws FormatException {
//        // This error should never occur because we shouldn't be able to parse a message that includes multidimensional arrays
//        throw new UnsupportedOperationException("ROS does not support multidimensional arrays (this error should not occur)");
//    }
    
    @Override
    public String toString() {
        return "rostype{" + getName() + "}";
    }

}
