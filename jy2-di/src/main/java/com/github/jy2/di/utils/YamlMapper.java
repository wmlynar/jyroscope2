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
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class YamlMapper {

	static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

	static {
		mapper.registerModule(new Jdk8Module());
		mapper.registerModule(new JavaTimeModule());
	}

	public static String map(Object obj) throws IOException {
		return mapper.writeValueAsString(obj);
	}

	public static void map(Object obj, File jsonFile) throws IOException {
		mapper.writeValue(jsonFile, obj);
	}

	public static <T> T map(String json, Class<T> clazz) throws IOException {
		return YamlMapper.mapper.readValue(json, clazz);
	}

	public static <T> T map(File jsonFile, Class<T> clazz) throws IOException {
		return YamlMapper.mapper.readValue(jsonFile, clazz);
	}

	public static String mapWithRuntimeException(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Problem with serializing " + obj.getClass(), e);
		}
	}

	public static void mapWithRuntimeException(Object obj, File jsonFile) {
		try {
			mapper.writeValue(jsonFile, obj);
		} catch (IOException e) {
			throw new RuntimeException("Problem with serializing " + obj.getClass() + " to file: " + jsonFile, e);
		}
	}

	public static <T> T mapWithRuntimeException(String json, Class<T> clazz) {
		try {
			return YamlMapper.mapper.readValue(json, clazz);
		} catch (IOException e) {
			throw new RuntimeException("Problem with parsing " + clazz.getCanonicalName() + ": " + json, e);
		}
	}

	public static <T> T mapWithRuntimeException(File jsonFile, Class<T> clazz) {
		try {
			return YamlMapper.mapper.readValue(jsonFile, clazz);
		} catch (IOException e) {
			throw new RuntimeException("Problem with parsing " + clazz.getCanonicalName() + ": " + jsonFile, e);
		}
	}
}
