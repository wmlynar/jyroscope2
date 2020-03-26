package com.github.jy2.serialization.java;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import com.github.jy2.mapper.RosTypeConverters;
import com.jyroscope.types.TypeConverter;

import go.jyroscope.ros.jy2_msgs.JavaObject;

public class SerializationRosToJavaConverter<S extends D, D> extends TypeConverter<S, D> {

	TypeConverter converter;

	@Override
	public D convert(S source) {
		if (converter == null) {
			converter = RosTypeConverters.fromRosClass(JavaObject.class);
		}
		JavaObject obj = (JavaObject) converter.convert(source);
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			ByteArrayInputStream byis = new ByteArrayInputStream(obj.data);
			ObjectInputStream ois;
			if(classLoader!=null) {
				ois = new CustomObjectInputStream(classLoader, byis);
			} else {
				ois = new ObjectInputStream(byis);
			}
			return (D) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException("Problem with deserializing object", e);
		}
	}

}
