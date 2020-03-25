package com.jyroscope.ros.types;

import com.jyroscope.types.TypeConverterHelper;
import com.thoughtworks.qdox.model.JavaType;

public class RosConvertString implements TypeConverterHelper<RosType, JavaType> {

    private static final String readString = "#1.getString()";
    private static final String readStringBytes = "#1.getStringBytes()";
    private static final String writeString = "#1.putString(#2);";
    private static final String writeStringBytes = "#1.putStringBytes(#2);";
    
    @Override
	public String getReader(RosType from, JavaType to) {
        if (!(from instanceof RosStringType))
            return null;
        
		if (to.getFullyQualifiedName().equals("java.lang.String"))
            return readString;
		else if (to.getFullyQualifiedName().equals("byte[]"))
            return readStringBytes;
        
        return null;
    }

    @Override
	public String getWriter(JavaType from, RosType to) {
        if (!(to instanceof RosStringType))
            return null;
        
		if (from.getFullyQualifiedName().equals("java.lang.String"))
            return writeString;
		else if (from.getFullyQualifiedName().equals("byte[]"))
            return writeStringBytes;
        
        return null;
    }

    
}
