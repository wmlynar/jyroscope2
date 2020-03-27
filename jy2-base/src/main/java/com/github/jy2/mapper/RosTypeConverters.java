package com.github.jy2.mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import com.jyroscope.annotations.Initializer;
import com.jyroscope.local.types.IdentityTypeConverter;
import com.jyroscope.ros.RosMessage;
import com.jyroscope.types.ConversionException;
import com.jyroscope.types.TypeConverter;

//import io.github.classgraph.ClassGraph;
//import io.github.classgraph.ClassInfo;
//import io.github.classgraph.ScanResult;

public class RosTypeConverters {

	public static final IdentityTypeConverter IDENTITY_TYPE_CONVERTER = new IdentityTypeConverter();

	public static HashMap<String, TypeConverter> fromRosName = new HashMap<>();
	public static HashMap<String, TypeConverter> toRosName = new HashMap<>();
	public static HashMap<Class<?>, TypeConverter> fromRosClass = new HashMap<>();
	public static HashMap<Class<?>, TypeConverter> toRosClass = new HashMap<>();
	public static HashMap<Class<?>, String> classToTopic = new HashMap<>();
	public static HashMap<String, Class<?>> topicToClass = new HashMap<>();
	public static HashMap<String, String> md5Map = new HashMap<>();
	public static HashMap<String, String> definitionMap = new HashMap<>();
	public static HashMap<String, Integer> sizeMap = new HashMap<>();
	public static HashMap<Class<?>, Boolean> isInitializedClassMap = new HashMap<>();
	public static HashMap<String, Boolean> isInitializedRosMap = new HashMap<>();

	public static <D> TypeConverter<RosMessage, D> fromRosMessage(String rosType) {
		return fromRosName.get(rosType);
	}

	public static <D> TypeConverter<D, RosMessage> toRosMessage(String rosType) {
		return toRosName.get(rosType);
	}

	public static <D> TypeConverter<RosMessage, D> fromRosClass(Class<?> type) {
		return fromRosClass.get(type);
	}

	public static <D> TypeConverter<D, RosMessage> toRosClass(Class<?> type) {
		return toRosClass.get(type);
	}

	public static TypeConverter get(Class<?> fromType, Class<?> toType) {
		if (fromType.equals(RosMessage.class)) {
			if (toType.equals(RosMessage.class)) {
				return IDENTITY_TYPE_CONVERTER;
			} else {
				return RosTypeConverters.fromRosClass(toType);
			}
		} else if (toType.equals(RosMessage.class)) {
			return RosTypeConverters.toRosClass(fromType);
		} else {
			return IDENTITY_TYPE_CONVERTER;
		}
	}

	public static String getRosType(Class<?> messageType) {
		return classToTopic.get(messageType);
	}

	public static Class<?> getRosType(String messageType) {
		return topicToClass.get(messageType);
	}

	public static String getMd5(String rosType) {
		return md5Map.get(rosType);
	}

	public static String getDefinition(String rosType) {
		return definitionMap.get(rosType);
	}

	public static int getSize(String rosType) {
		Integer size = sizeMap.get(rosType);
		if (size == null) {
			return -1;
		}
		return size;
	}

	public static <A, B> void register(String rosTypeName, Class<?> type, TypeConverter from, TypeConverter to,
			String md5, int size, String definition) {
		fromRosName.put(rosTypeName, from);
		fromRosClass.put(type, from);
		toRosName.put(rosTypeName, to);
		toRosClass.put(type, to);
		md5Map.put(rosTypeName, md5);
		definitionMap.put(rosTypeName, definition);
		classToTopic.put(type, rosTypeName);
		topicToClass.put(rosTypeName, type);
		sizeMap.put(rosTypeName, size);
	}

	public static <A, B> void registerPrimitive(String rosTypeName, Class<?> type, TypeConverter from,
			TypeConverter to) {
		fromRosClass.put(type, from);
		toRosClass.put(type, to);
		classToTopic.put(type, rosTypeName);
	}

	public static <A> void precompile(Class<A> type) throws ConversionException {
		if (isInitializedClassMap.get(type) != null) {
			return;
		}
		String initializer = "com.jyroscope.initializers." + type.getName().replace(".", "_");
		try {
			Class<?> clazz = Class.forName(initializer);
			Method method = clazz.getMethod("initialize");
			method.invoke(null);
			// mark precompilation by topic name as done
			String typeName = classToTopic.get(type);
			isInitializedRosMap.put(typeName, true);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new ConversionException("Cannot find ROS type for class " + type.getName());
		} finally {
			isInitializedClassMap.put(type, true);
		}
	}

	public static void precompileByRosName(String typeName) throws ConversionException {
		if (isInitializedRosMap.get(typeName) != null) {
			return;
		}
		String initializer = "com.jyroscope.initializers2." + typeName.replace("/", "_");
		try {
			Class<?> clazz = Class.forName(initializer);
			Method method = clazz.getMethod("initialize");
			method.invoke(null);
			// mark precompilation by topic name as done
			Class<?> c = topicToClass.get(typeName);
			isInitializedClassMap.put(c, true);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new ConversionException("Cannot find initializer to ROS type " + typeName);
		} finally {
			isInitializedRosMap.put(typeName, true);
		}
	}

//	public static void scanAnnotationsAndInitialize() {
//		try (ScanResult result = new ClassGraph().enableClassInfo().enableAnnotationInfo().scan()) {
//			for (ClassInfo info : result.getClassesWithAnnotation(Initializer.class.getName())) {
//				try {
//					Class<?> clazz = info.loadClass();
//					Method method = clazz.getMethod("initialize");
//					method.invoke(null);
//				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
//						| InvocationTargetException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}
//
//	/**
//	 * Scans for all resources in jyroscope2 directory and loads classes with those
//	 * resource names.
//	 */
//	public static void scanResourcesAndInitialize() {
//		try (InputStream in = getResourceAsStream("/jyroscope2");
//				InputStreamReader isr = new InputStreamReader(in);
//				BufferedReader br = new BufferedReader(isr)) {
//			String resource;
//			while ((resource = br.readLine()) != null) {
//				try {
//					Class<?> clazz = Class.forName(resource);
//					Method method = clazz.getMethod("initialize");
//					method.invoke(null);
//				} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
//						| IllegalArgumentException | InvocationTargetException e) {
//					e.printStackTrace();
//				}
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//	private static InputStream getResourceAsStream(String resource) {
//		final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
//		return in == null ? RosTypeConverters.class.getResourceAsStream(resource) : in;
//	}

}
