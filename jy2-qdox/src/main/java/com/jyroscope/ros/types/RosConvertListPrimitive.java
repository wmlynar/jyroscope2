package com.jyroscope.ros.types;

import java.util.Arrays;
import java.util.List;

import com.jyroscope.core.types.TypeConverterHelper;
import com.thoughtworks.qdox.model.JavaType;

public class RosConvertListPrimitive implements TypeConverterHelper<RosType, JavaType> {

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
    
    private static final String[] readArray = {
        /*bool*/      "#1.getByte(#2)",
        /*int8*/      "#1.getByte(#2)",
        /*int16*/     "#1.getShort(#2)",
        /*int32*/     "#1.getInt(#2)",
        /*int64*/     "#1.getLong(#2)",
        /*uint8*/     "#1.getByte(#2)",
        /*uint16*/    "#1.getShort(#2)",
        /*uint32*/    "#1.getInt(#2)",
        /*uint64*/    "#1.getLong(#2)",
        /*float32*/   "#1.getFloat(#2)",
        /*float64*/   "#1.getDouble(#2)",
        /*duration*/  "#1.getDuration(#2)",
        /*time*/      "#1.getInstant(#2)"
    };
    
    private static final String[] writeArray = {
        /*bool*/      "#1.putByte(#2, #3);",
        /*int8*/      "#1.putByte(#2, #3);",
        /*int16*/     "#1.putShort(#2, #3);",
        /*int32*/     "#1.putInt(#2, #3);",
        /*int64*/     "#1.putLong(#2, #3);",
        /*uint8*/     "#1.putByte(#2, #3);",
        /*uint16*/    "#1.putShort(#2, #3);",
        /*uint32*/    "#1.putInt(#2, #3);",
        /*uint64*/    "#1.putLong(#2, #3);",
        /*float32*/   "#1.putFloat(#2, #3);",
        /*float64*/   "#1.putDouble(#2, #3);",
        /*duration*/  "#1.putDuration(#2, #3);",
        /*time*/      "#1.putInstant(#2, #3);",
    };
    
    private static final String[] arrayNatives = {
        /*bool*/      "byte[]",
        /*int8*/      "byte[]",
        /*int16*/     "short[]",
        /*int32*/     "int[]",
        /*int64*/     "long[]",
        /*uint8*/     "byte[]",
        /*uint16*/    "short[]",
        /*uint32*/    "int[]",
        /*uint64*/    "long[]",
        /*float32*/   "float[]",
        /*float64*/   "double[]",
        /*duration*/  "java.time.Duration[]",
        /*time*/      "Instant[]"
    };
    
    private static final String readString = "#1.getString()";
    private static final String writeString = "#1.putString(#2);";
    
	public static String getPrimitiveArrayType(RosPrimitiveType type) {
        int ros = rosPrimitives.indexOf(type.getCanonicalType());
        return arrayNatives[ros];
    }
    
    public static String getPrimitiveArrayReader(RosPrimitiveType type) {
        int ros = rosPrimitives.indexOf(type.getCanonicalType());
        return readArray[ros];
    }
    
    public static String getPrimitiveArrayWriter(RosPrimitiveType type) {
        int ros = rosPrimitives.indexOf(type.getCanonicalType());
        return writeArray[ros];
    }

    @Override
	public String getReader(RosType from, JavaType to) {
        if (!(from instanceof RosListType))
            return null;
        RosListType list = (RosListType)from;

        RosType baseType = list.getBaseType();
        if (!(baseType instanceof RosPrimitiveType))
            return null;
        
        RosPrimitiveType primitive = (RosPrimitiveType)baseType;
        primitive = primitive.getCanonicalType();
        
        int ros = rosPrimitives.indexOf(primitive);
        if (ros == -1)
            return null;
        
		if (arrayNatives[ros].equals(to.getFullyQualifiedName())) {
            int length = list.getDeclaredLength();
            return readArray[ros].replace("#2", String.valueOf(length));
        }
        
		if ("java.lang.String".equals(to.getFullyQualifiedName())
				&& (primitive == RosPrimitiveType.Int8 || primitive == RosPrimitiveType.UInt8))
            return readString;
        
        return null;
    }

    @Override
	public String getWriter(JavaType from, RosType to) {
        if (!(to instanceof RosListType))
            return null;
        RosListType list = (RosListType)to;

        RosType baseType = list.getBaseType();
        if (!(baseType instanceof RosPrimitiveType))
            return null;
        
        RosPrimitiveType primitive = (RosPrimitiveType)baseType;
        primitive = primitive.getCanonicalType();
        
        int ros = rosPrimitives.indexOf(primitive);
        if (ros == -1)
            return null;
        
		if (arrayNatives[ros].equals(from.getFullyQualifiedName())) {
            int length = list.getDeclaredLength();
            return writeArray[ros].replace("#3", String.valueOf(length));
        }
        
		if ("java.lang.String".equals(from.getFullyQualifiedName())
				&& (primitive == RosPrimitiveType.Int8 || primitive == RosPrimitiveType.UInt8))
            return writeString;
        
        return null;
    }
    
}
