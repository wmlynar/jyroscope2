package com.github.jy2.serialization.fst;

import com.github.jy2.mapper.RosTypeConverters;
import com.jyroscope.types.TypeConverter;

import go.jyroscope.ros.jy2_msgs.JavaObject;

public class FstJavaToRosConverter<S extends D, D> extends TypeConverter<S, D> {

	static TypeConverter converter;

	static {
		converter = RosTypeConverters.toRosClass(JavaObject.class);
	}

	@Override
	public D convert(S source) {
		JavaObject obj = new JavaObject();
		obj.data = FstRosToJavaConverter.configuration.asByteArray(source);
		return (D) converter.convert(obj);
	}

}
