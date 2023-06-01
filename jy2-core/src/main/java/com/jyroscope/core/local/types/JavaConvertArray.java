package com.jyroscope.core.local.types;

import com.jyroscope.core.types.TypeConverterHelper;

public class JavaConvertArray implements TypeConverterHelper<Class<?>, Class<?>> {

    private static final String identity = "#1";
    
    @Override
    public String getReader(Class<?> from, Class<?> to) {
        if (from.equals(to))
            return identity;
        else
            return null;
    }

    @Override
    public String getWriter(Class<?> from, Class<?> to) {
        return null;
    }

}