package com.jyroscope.local.types;

import com.jyroscope.types.*;

public class JavaConvertString implements TypeConverterHelper<Class<?>, Class<?>> {

    private static final String CHARSET = "com.jyroscope.Encoding.CHARSET";
    private static final String identity = "#1";
    private static final String bytesToString = "new String(#1, " + CHARSET + ")";
    private static final String stringToBytes = "#1.getBytes(" + CHARSET + ")";
    
    @Override
    public String getReader(Class<?> from, Class<?> to) {
        if (from.equals(to))
            return identity;
        
        if (byte[].class.equals(from) && String.class.equals(to))
            return bytesToString;
        
        else if (String.class.equals(from) && byte[].class.equals(to))
            return stringToBytes;
        
        return null;
    }

    @Override
    public String getWriter(Class<?> from, Class<?> to) {
        return null;
    }

}