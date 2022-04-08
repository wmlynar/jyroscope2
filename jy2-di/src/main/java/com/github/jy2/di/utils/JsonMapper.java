/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jy2.di.utils;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonMapper {

	static ObjectMapper mapper;

	static {
		mapper.registerModule(new Jdk8Module());
		mapper.registerModule(new JavaTimeModule());
	}

	public static String map(Object obj) throws IOException {
		if (JsonMapper.mapper == null) {
			JsonMapper.mapper = new ObjectMapper();
		}

		return mapper.writeValueAsString(obj);
	}

	public static void map(Object obj, File file) throws IOException {
		if (JsonMapper.mapper == null) {
			JsonMapper.mapper = new ObjectMapper();
		}

		mapper.writeValue(file, obj);
	}

	public static <T> T map(String json, Class<T> clazz) throws IOException {
		if (JsonMapper.mapper == null) {
			JsonMapper.mapper = new ObjectMapper();
		}

		return JsonMapper.mapper.readValue(json, clazz);
	}

	public static <T> T map(File jsonFile, Class<T> clazz) throws IOException {
		if (JsonMapper.mapper == null) {
			JsonMapper.mapper = new ObjectMapper();
		}

		return JsonMapper.mapper.readValue(jsonFile, clazz);
	}

	public static String mapWithRuntimeException(Object obj) {
		if (JsonMapper.mapper == null) {
			JsonMapper.mapper = new ObjectMapper();
		}

		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Problem with serializing " + obj.getClass(), e);
		}
	}

	public static void mapWithRuntimeException(Object obj, File file) {
		if (JsonMapper.mapper == null) {
			JsonMapper.mapper = new ObjectMapper();
		}

		try {
			mapper.writeValue(file, obj);
		} catch (IOException e) {
			throw new RuntimeException("Problem with serializing " + obj.getClass() + " to file: " + file, e);
		}
	}

	public static <T> T mapWithRuntimeException(String json, Class<T> clazz) {
		if (JsonMapper.mapper == null) {
			JsonMapper.mapper = new ObjectMapper();
		}

		try {
			return JsonMapper.mapper.readValue(json, clazz);
		} catch (IOException e) {
			throw new RuntimeException("Problem with parsing " + clazz.getCanonicalName() + ": " + json, e);
		}
	}

	public static <T> T mapWithRuntimeException(String json, TypeReference<T> typeReference) {
		if (JsonMapper.mapper == null) {
			JsonMapper.mapper = new ObjectMapper();
		}

		try {
			return JsonMapper.mapper.readValue(json, typeReference);
		} catch (IOException e) {
			throw new RuntimeException("Problem with parsing " + typeReference.toString() + ": " + json, e);
		}
	}

	public static <T> T mapWithRuntimeException(File jsonFile, Class<T> clazz) {
		if (JsonMapper.mapper == null) {
			JsonMapper.mapper = new ObjectMapper();
		}

		try {
			return JsonMapper.mapper.readValue(jsonFile, clazz);
		} catch (IOException e) {
			throw new RuntimeException("Problem with parsing " + clazz.getCanonicalName() + ": " + jsonFile, e);
		}
	}

	public static <T> T mapWithRuntimeException(File jsonFile, TypeReference<T> typeReference) {
		if (JsonMapper.mapper == null) {
			JsonMapper.mapper = new ObjectMapper();
		}

		try {
			return JsonMapper.mapper.readValue(jsonFile, typeReference);
		} catch (IOException e) {
			throw new RuntimeException("Problem with parsing " + typeReference.toString() + ": " + jsonFile, e);
		}
	}
}
