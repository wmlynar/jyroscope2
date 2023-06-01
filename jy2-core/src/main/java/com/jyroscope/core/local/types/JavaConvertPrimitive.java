package com.jyroscope.core.local.types;

import com.jyroscope.core.types.TypeConverterHelper;
import java.util.*;

public class JavaConvertPrimitive implements TypeConverterHelper<Class<?>, Class<?>> {

    private static final String identity = "#1";
    
    private static final List<Class<?>> javaPrimitives = Arrays.asList(new Class<?>[] {
        boolean.class, 
        byte.class, 
        char.class, 
        short.class, 
        int.class, 
        long.class, 
        float.class, 
        double.class,
    });
    
    private static final String[][] javaToJava = {
                      /*boolean*/ /*byte*/           /*char*/           /*short*/           /*int*/      /*long*/      /*float*/     /*double*/
        /*boolean*/   {"#1",      "(byte)(#1?-1:0)", "(char)(#1?-1:0)", "(short)(#1?-1:0)", "(#1?-1:0)",  "(#1?-1:0)",  "(#1?-1:0)",  "(#1?-1:0)"},
        /*byte*/      {null,      "(byte)(#1)",      "(char)(#1)",      "(short)(#1)",      "#1",        "#1",         "#1",         "#1"},
        /*char*/      {null,      null,              "(char)(#1)",      null,               "#1",        "#1",         "#1",         "#1"},
        /*short*/     {null,      null,              "(char)(#1)",      "(short)(#1)",      "#1",        "#1",         "#1",         "#1"},
        /*int*/       {null,      null,              null,              null,               "#1",        "#1",         "#1",         "#1"},
        /*long*/      {null,      null,              null,              null,               null,        "#1",         "#1",         "#1"},
        /*float*/     {null,      null,              null,              null,               null,        null,         "#1",         "#1"},
        /*double*/    {null,      null,              null,              null,               null,        null,         null,         "#1"}
    };
    
    
    @Override
    public String getReader(Class<?> from, Class<?> to) {
        if (from.equals(to))
            return identity;
        
        if (!from.isPrimitive() || !to.isPrimitive())
            return null;
        
        int fromIndex = javaPrimitives.indexOf(from);
        int toIndex = javaPrimitives.indexOf(to);
        
        return javaToJava[fromIndex][toIndex];
    }

    @Override
    public String getWriter(Class<?> from, Class<?> to) {
        return null;
    }

}
