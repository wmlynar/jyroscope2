package com.github.core.serialization;

import java.io.Serializable;
import java.util.HashSet;

import com.github.core.serialization.fst.FstJavaToRosConverter;
import com.github.core.serialization.fst.FstRosToJavaConverter;
import com.github.jy2.mapper.RosTypeConverters;
import com.jyroscope.annotations.Message;
import com.jyroscope.ros.RosMessage;
import com.jyroscope.types.ConversionException;
import com.jyroscope.types.TypeConverter;

import go.jyroscope.ros.jy2_msgs.JavaObject;

/**
 * Wrapper dedicated for sending java objects in ros mesages.
 *
 */
public class RosTypeConvertersSerializationWrapper {

	private static final TypeConverter ROS_TO_JAVA_CONVERTER = new FstRosToJavaConverter<>();
	private static final TypeConverter JAVA_TO_ROS_CONVERTER = new FstJavaToRosConverter<>();

	private static final HashSet<Class<?>> javaTypes = new HashSet<>();
	
	static {
		// hack to be able to deserialize java objects published by rosbag play (rosbags do not record headers)
		javaTypes.add(go.jyroscope.ros.jy2_msgs.JavaObject.class);
	}

	public static boolean isJavaType(Class<?> type) {
		boolean hasAnnotation = type.getAnnotation(Message.class) != null;
		boolean isSerializable = Serializable.class.isAssignableFrom(type);
		return !hasAnnotation && isSerializable;
	}

	public static <A> void precompile(Class<A> type) throws ConversionException {
		if (javaTypes.contains(type)) {
			return;
		}
		try {
			RosTypeConverters.precompile(type);
		} catch (ConversionException e) {
			if (isJavaType(type)) {
				javaTypes.add(type);
				return;
			}
			throw e;
		}
	}

	public static TypeConverter get(Class<?> fromType, Class<?> toType) {
		if (fromType.equals(RosMessage.class) && javaTypes.contains(toType)) {
			return ROS_TO_JAVA_CONVERTER;
		} else if (toType.equals(RosMessage.class) && javaTypes.contains(fromType)) {
			return JAVA_TO_ROS_CONVERTER;
		}

		return RosTypeConverters.get(fromType, toType);
	}

	public static String getRosType(Class<?> messageType) {
		if (javaTypes.contains(messageType)) {
			return RosTypeConverters.getRosType(JavaObject.class);
		} else {
			return RosTypeConverters.getRosType(messageType);
		}
	}

	public static String getJavaType(Class<?> messageType) {
		if (javaTypes.contains(messageType)) {
			return messageType.getName();
		} else {
			return null;
		}
	}

}
