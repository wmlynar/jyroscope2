package com.jyroscope.ros.types;

import com.jyroscope.types.TypeConverterHelper;
import java.time.*;
import java.util.*;

public class RosConvertPrimitive implements TypeConverterHelper<RosType, Class<?>> {
    
    private static final List<RosPrimitiveType> rosPrimitives = Arrays.asList(new RosPrimitiveType[] { 
        /* 0*/ RosPrimitiveType.Bool,
        /* 1*/ RosPrimitiveType.Int8,
        /* 2*/ RosPrimitiveType.Int16,
        /* 3*/ RosPrimitiveType.Int32,
        /* 4*/ RosPrimitiveType.Int64,
        /* 5*/ RosPrimitiveType.UInt8,
        /* 6*/ RosPrimitiveType.UInt16,
        /* 7*/ RosPrimitiveType.UInt32,
        /* 8*/ RosPrimitiveType.UInt64,
        /* 9*/ RosPrimitiveType.Float32,
        /*10*/ RosPrimitiveType.Float64,
        /*11*/ RosPrimitiveType.Duration,
        /*12*/ RosPrimitiveType.Time
    });
    
    private static final List<Class<?>> javaPrimitives = Arrays.asList(new Class<?>[] {
        boolean.class, 
        byte.class, 
        char.class, 
        short.class, 
        int.class, 
        long.class, 
        float.class, 
        double.class,
        Duration.class,
        Instant.class
    });
    
    private static final String identity = "#1";
    
    private static final String[][] rosToJava = {
                      /*boolean*/ /*byte*/      /*char*/           /*short*/           /*int*/        /*long*/            /*float*/           /*double*/          /*Duration*/ /*Instant*/
        /*bool*/      {"(#1!=0)", "(byte)(#1)", null,              "(short)(#1&0xff)", "(#1&0xff)",    "(#1&0xff)",       "(#1&0xff)",        "(#1&0xff)",        null,        null},
        /*int8*/      {null,      "(byte)(#1)", null,              "(short)(#1)",      "#1",          "#1",               "#1",               "#1",               null,        null},
        /*int16*/     {null,      null,         null,              "(short)(#1)",      "#1",          "#1",               "#1",               "#1",               null,        null},
        /*int32*/     {null,      null,         null,              null,               "#1",          "#1",               "#1",               "#1",               null,        null},
        /*int64*/     {null,      null,         null,              null,               null,          "#1",               "#1",               "#1",               null,        null},
        /*uint8*/     {null,      "(byte)(#1)", "(char)(#1&0xff)", "(short)(#1&0xff)", "(#1&0xff)",   "(#1&0xff)",        "(#1&0xff)",        "(#1&0xff)",        null,        null},
        /*uint16*/    {null,      null,         "(char)(#1)",      "(short)(#1)",      "(#1&0xffff)", "(#1&0xffff)",      "(#1&0xffff)",      "(#1&0xffff)",      null,        null},
        /*uint32*/    {null,      null,         null,              null,               "#1",          "(#1&0xffffffffL)", "(#1&0xffffffffL)", "(#1&0xffffffffL)", null,        null},
        /*uint64*/    {null,      null,         null,              null,               null,          "#1",               "long #t = #1; #= (#t >= 0 ? (float)(#t&0x7fffffffffffffffL):(float)(#t&0x7fffffffffffffffL)+0x1.0p63f)", "long #t = #1; #= (#t >= 0 ? (float)(#t&0x7fffffffffffffffL):(float)(#t&0x7fffffffffffffffL)+0x1.0p63f)", null, null},
        /*float32*/   {null,      null,         null,              null,               null,          null,               "#1",               "#1",               null,        null},
        /*float64*/   {null,      null,         null,              null,               null,          null,               null,               "#1",               null,        null},
        /*duration*/  {null,      null,         null,              null,               null,          null,               null,               null,               "#1",        null},     
        /*time*/      {null,      null,         null,              null,               null,          null,               null,               null,               null,        "#1"}     
    };
    
    private static final String[][] javaToRos = {
                      /*boolean*/          /*byte*/ /*char*/       /*short*/ /*int*/ /*long*/ /*float*/ /*double*/ /*Duration*/ /*Instant*/
        /*bool*/      {"(byte)(#2?-1:0)",  null,    null,          null,     null,   null,    null,     null,      null,        null},              
        /*int8*/      {"(byte)(#2?-1:0)",  "#2",    null,          null,     null,   null,    null,     null,      null,        null},              
        /*int16*/     {"(short)(#2?-1:0)", "#2",    null,          "#2",     null,   null,    null,     null,      null,        null},              
        /*int32*/     {"(#2?-1:0)",        "#2",    "#2",          "#2",     "#2",   null,    null,     null,      null,        null},              
        /*int64*/     {"(#2?-1:0)",        "#2",    "#2",          "#2",     "#2",   "#2",    "#2",     "#2",      null,        null},                 
        /*uint8*/     {"(byte)(#2?-1:0)",  "#2",    null,          null,     null,   null,    null,     null,      null,        null},              
        /*uint16*/    {"(short)(#2?-1:0)", "#2",    "(short)(#2)", "#2",     null,   null,    null,     null,      null,        null},              
        /*uint32*/    {"(#2?-1:0)",        "#2",    "#2",          "#2",     "#2",   null,    null,     null,      null,        null},              
        /*uint64*/    {"(#2?-1:0)",        "#2",    "#2",          "#2",     "#2",   "#2",    null,     null,      null,        null},              
        /*float32*/   {"(#2?-1:0)",        "#2",    "#2",          "#2",     "#2",   "#2",    "#2",     null,      null,        null},              
        /*float64*/   {"(#2?-1:0)",        "#2",    "#2",          "#2",     "#2",   "#2",    "#2",     "#2",      null,        null}, 
        /*duration*/  {null,               null,    null,          null,     null,   null,    null,     null,      "#2",        null},
        /*time*/      {null,               null,    null,          null,     null,   null,    null,     null,      null,        "#2"}
    };
    
    private static final String[] readField = {
        /*bool*/      "#1.getByte()",
        /*int8*/      "#1.getByte()",
        /*int16*/     "#1.getShort()",
        /*int32*/     "#1.getInt()",
        /*int64*/     "#1.getLong()",
        /*uint8*/     "#1.getByte()",
        /*uint16*/    "#1.getShort()",
        /*uint32*/    "#1.getInt()",
        /*uint64*/    "#1.getLong()",
        /*float32*/   "#1.getFloat()",
        /*float64*/   "#1.getDouble()",
        /*duration*/  "#1.getDuration()",
        /*time*/      "#1.getInstant()"
    };
    
    private static final String[] writeField = {
        /*bool*/      "#1.put(#2);",
        /*int8*/      "#1.put(#2);",
        /*int16*/     "#1.putShort(#2);",
        /*int32*/     "#1.putInt(#2);",
        /*int64*/     "#1.putLong(#2);",
        /*uint8*/     "#1.put(#2);",
        /*uint16*/    "#1.putShort(#2);",
        /*uint32*/    "#1.putInt(#2);",
        /*uint64*/    "#1.putLong(#2);",
        /*float32*/   "#1.putFloat(#2);",
        /*float64*/   "#1.putDouble(#2);",
        /*duration*/  "#1.putDuration(#2);",
        /*time*/      "#1.putInstant(#2);"
    };
    
    public static String getPrimitiveReader(RosPrimitiveType type) {
        int ros = rosPrimitives.indexOf(type.getCanonicalType());
        return readField[ros];
    }
    
    public static String getPrimitiveWriter(RosPrimitiveType type) {
        int ros = rosPrimitives.indexOf(type.getCanonicalType());
        return writeField[ros];
    }
    
    public static String getCast(RosPrimitiveType from, Class<?> to) {
        // Handle Duration and Time (only identity casts)
        if (from == RosPrimitiveType.Duration && Duration.class.equals(to))
            return identity;
        else if (from == RosPrimitiveType.Time && Instant.class.equals(to))
            return identity;
        
        // Handle the numerical types
        int ros = rosPrimitives.indexOf(from.getCanonicalType());
        int java = javaPrimitives.indexOf(to);
        if (ros == -1 || java == -1)
            return null;
        else
            return rosToJava[ros][java];
    }
    
    public static String getCast(Class<?> from, RosPrimitiveType to) {
        int java = javaPrimitives.indexOf(from);
        int ros = rosPrimitives.indexOf(to.getCanonicalType());
        if (ros == -1 || java == -1)
            return null;
        else
            return javaToRos[ros][java];
    }

    @Override
    public String getReader(RosType from, Class<?> to) {
        if (!(from instanceof RosPrimitiveType))
            return null;
        RosPrimitiveType primitive = (RosPrimitiveType)from;
        primitive = primitive.getCanonicalType();
        
        int ros = rosPrimitives.indexOf(primitive);
        int java = javaPrimitives.indexOf(to);
        
        if (ros == -1 || java == -1)
            return null;
        
        String reader = readField[ros];
        String cast = rosToJava[ros][java];
        
        if (cast == null)
            return null;
        
        return cast.replace("#1", reader);
    }

    @Override
    public String getWriter(Class<?> from, RosType to) {
        if (!(to instanceof RosPrimitiveType))
            return null;
        RosPrimitiveType primitive = (RosPrimitiveType)to;
        primitive = primitive.getCanonicalType();
        
        int ros = rosPrimitives.indexOf(primitive);
        int java = javaPrimitives.indexOf(from);
        
        if (ros == -1 || java == -1)
            return null;
        
        String writer = writeField[ros];
        String cast = javaToRos[ros][java];
        
        return writer.replace("#2", cast);
    }

}
