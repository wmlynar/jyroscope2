package com.github.jy2.serialization.java;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.github.jy2.mapper.RosTypeConverters;
import com.github.jy2.serialization.fst.FstCodec;
import com.jyroscope.types.TypeConverter;

import go.jyroscope.ros.jy2_msgs.JavaObject;

public class SerializationJavaToRosConverter<S extends D, D> extends TypeConverter<S, D> {

	TypeConverter converter;
	FstCodec configuration;

	@Override
	public D convert(S source) {
		if (converter == null) {
			converter = RosTypeConverters.toRosClass(JavaObject.class);
			configuration = new FstCodec();
		}
		JavaObject obj = new JavaObject();
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos)) {
			oos.writeObject(source);
			oos.close();
			obj.data = baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Problem with serializing object", e);
		}
		return (D) converter.convert(obj);
	}

}
