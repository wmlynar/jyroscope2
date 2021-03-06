/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jy2.di.utils;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class YamlMapper {

	static ObjectMapper mapper;

	public static String map(Object obj) throws IOException {
		requireMapper();

		return mapper.writeValueAsString(obj);
	}

	public static void map(Object obj, File jsonFile) throws IOException {
		requireMapper();

		mapper.writeValue(jsonFile, obj);
	}

	public static <T> T map(String json, Class<T> clazz) throws IOException {
		requireMapper();

		return YamlMapper.mapper.readValue(json, clazz);
	}

	public static <T> T map(File jsonFile, Class<T> clazz) throws IOException {
		requireMapper();

		return YamlMapper.mapper.readValue(jsonFile, clazz);
	}

	static void requireMapper() {
		if (YamlMapper.mapper == null) {
			YamlMapper.mapper = new ObjectMapper(new YAMLFactory());
		}
	}

	public static String mapWithRuntimeException(Object obj) {
		requireMapper();

		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Problem with serializing " + obj.getClass(), e);
		}
	}

	public static void mapWithRuntimeException(Object obj, File jsonFile) {
		requireMapper();

		try {
			mapper.writeValue(jsonFile, obj);
		} catch (IOException e) {
			throw new RuntimeException("Problem with serializing " + obj.getClass() + " to file: " + jsonFile, e);
		}
	}

	public static <T> T mapWithRuntimeException(String json, Class<T> clazz) {
		requireMapper();

		try {
			return YamlMapper.mapper.readValue(json, clazz);
		} catch (IOException e) {
			throw new RuntimeException("Problem with parsing " + clazz.getCanonicalName() + ": " + json, e);
		}
	}

	public static <T> T mapWithRuntimeException(File jsonFile, Class<T> clazz) {
		requireMapper();

		try {
			return YamlMapper.mapper.readValue(jsonFile, clazz);
		} catch (IOException e) {
			throw new RuntimeException("Problem with parsing " + clazz.getCanonicalName() + ": " + jsonFile, e);
		}
	}
}
