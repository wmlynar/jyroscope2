package com.jyroscope.roscore.types;

import com.jyroscope.core.types.TypeConverterHelper;
import com.jyroscope.ros.types.RosStringType;
import com.jyroscope.ros.types.RosType;

public class RosConvertString implements TypeConverterHelper<RosType, Class<?>> {

    private static final String readString = "#1.getString()";
    private static final String readStringBytes = "#1.getStringBytes()";
    private static final String writeString = "#1.putString(#2);";
    private static final String writeStringBytes = "#1.putStringBytes(#2);";
    
    @Override
    public String getReader(RosType from, Class<?> to) {
        if (!(from instanceof RosStringType))
            return null;
        
        if (String.class.equals(to))
            return readString;
        else if (byte[].class.equals(to))
            return readStringBytes;
        
        return null;
    }

    @Override
    public String getWriter(Class<?> from, RosType to) {
        if (!(to instanceof RosStringType))
            return null;
        
        if (String.class.equals(from))
            return writeString;
        else if (byte[].class.equals(from))
            return writeStringBytes;
        
        return null;
    }

    
}
