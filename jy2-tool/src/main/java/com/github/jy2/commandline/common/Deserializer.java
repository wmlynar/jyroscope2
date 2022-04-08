package com.github.jy2.commandline.common;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Deserializer {

	private static ObjectMapper mapper = new ObjectMapper();
	
	static {
		mapper.registerModule(new Jdk8Module());
		mapper.registerModule(new JavaTimeModule());
	}

	public Deserializer() {
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

	public Object deserialize(Class<?> type, String string) {
		if (type.equals(Boolean.class)) {
			return Boolean.parseBoolean(string);
		} else if (type.equals(Character.class)) {
			return string.charAt(0);
		} else if (type.equals(Byte.class)) {
			return (byte) Integer.parseInt(string);
		} else if (type.equals(Short.class)) {
			return (short) Integer.parseInt(string);
		} else if (type.equals(Integer.class)) {
			return (int) Long.parseLong(string);
		} else if (type.equals(Long.class)) {
			return Long.parseLong(string);
		} else if (type.equals(Float.class)) {
			return Float.parseFloat(string);
		} else if (type.equals(Double.class)) {
			return Double.parseDouble(string);
		} else if (type.equals(String.class)) {
			return string;
		} else if (type.equals(Void.class)) {
			return "";
		}
		return mapWithRuntimeException(string, type);
	}

	public static <T> T mapWithRuntimeException(String json, Class<T> clazz) {
		try {
			return mapper.readValue(json, clazz);
		} catch (IOException e) {
			throw new RuntimeException("Problem with parsing " + clazz.getCanonicalName() + ": " + json, e);
		}
	}

}
