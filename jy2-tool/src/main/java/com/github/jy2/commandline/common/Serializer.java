package com.github.jy2.commandline.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Serializer {

	private static ObjectMapper mapper = new ObjectMapper();
	
	static {
		mapper.registerModule(new Jdk8Module());
		mapper.registerModule(new JavaTimeModule());
	}

	public Serializer() {
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

	public String serialize(Object t) {
		if (isPrimitive(t.getClass()) || t.getClass().equals(String.class)) {
			return t.toString();
		} else {
			return mapWithRuntimeException(t);
		}
	}

	private boolean isPrimitive(Class<?> type) {
		return type.equals(Boolean.class) || type.equals(Character.class) || type.equals(Byte.class)
				|| type.equals(Short.class) || type.equals(Integer.class) || type.equals(Long.class)
				|| type.equals(Float.class) || type.equals(Double.class) || type.equals(Void.class);
	}

	public static String mapWithRuntimeException(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Problem with serializing " + obj.getClass(), e);
		}
	}

}
