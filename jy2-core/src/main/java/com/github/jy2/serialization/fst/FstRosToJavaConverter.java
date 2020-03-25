package com.github.jy2.serialization.fst;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;

import com.github.jy2.JyroscopeCore;
import com.github.jy2.di.LogSeldom;
import com.github.jy2.mapper.RosTypeConverters;
import com.jyroscope.types.TypeConverter;

import go.jyroscope.ros.jy2_msgs.JavaObject;

public class FstRosToJavaConverter<S extends D, D> extends TypeConverter<S, D> {
	
	private final LogSeldom log = JyroscopeCore.getLog();

	static TypeConverter rosToJavaConverter;
	static FSTConfiguration config;
	
	static {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader != null) {
			config = new FstCodec(classLoader).getConfiguration();
		} else {
			config = new FstCodec().getConfiguration();
		}
		rosToJavaConverter = RosTypeConverters.fromRosClass(JavaObject.class);
	}

	@Override
	public D convert(S source) {
		JavaObject obj = (JavaObject) rosToJavaConverter.convert(source);
		if(obj.data==null || obj.data.length==0) {
			log.error("Illegal empty message!");
		}
		try (ByteArrayInputStream baos = new ByteArrayInputStream(obj.data);
				FSTObjectInput inputStream = config.getObjectInput(baos)) {
			return (D) inputStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException("Problem with deserializing object", e);
		}
	}

}
