package com.github.jy2.serialization.fst;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectOutput;

import com.github.jy2.mapper.RosTypeConverters;
import com.jyroscope.types.TypeConverter;

import go.jyroscope.ros.jy2_msgs.JavaObject;

public class FstJavaToRosConverter<S extends D, D> extends TypeConverter<S, D> {

	static TypeConverter converter;
	static FSTConfiguration config;

	static {
		config = new FstCodec().getConfiguration();
		converter = RosTypeConverters.toRosClass(JavaObject.class);
	}
	
	@Override
	public D convert(S source) {
		JavaObject obj = new JavaObject();
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            FSTObjectOutput oos = config.getObjectOutput(baos)) {
			oos.writeObject(source);
			oos.close();
			obj.data = baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Problem with serializing object", e);
		}
		return (D) converter.convert(obj);
	}

}
