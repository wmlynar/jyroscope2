package com.github.core.serialization.fst;

import org.nustaq.serialization.FSTConfiguration;

import com.github.jy2.mapper.RosTypeConverters;
import com.jyroscope.types.TypeConverter;

import go.jyroscope.ros.jy2_msgs.JavaObject;

public class FstRosToJavaConverter<S extends D, D> extends TypeConverter<S, D> {

	static TypeConverter rosToJavaConverter;
	static FSTConfiguration configuration;

	static {
		rosToJavaConverter = RosTypeConverters.fromRosClass(JavaObject.class);
		configuration = FSTConfiguration.createDefaultConfiguration();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader != null) {
			configuration.setClassLoader(classLoader);
		}
	}

	@Override
	public D convert(S source) {
		JavaObject obj = (JavaObject) rosToJavaConverter.convert(source);
		if (obj.data == null || obj.data.length == 0) {
			throw new RuntimeException("Illegal empty message!");
		}
		return (D) configuration.asObject(obj.data);
	}

}
