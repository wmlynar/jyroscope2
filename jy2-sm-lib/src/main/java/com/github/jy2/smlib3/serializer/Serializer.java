package com.github.jy2.smlib3.serializer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import com.github.jy2.smlib3.annotations.DontSerialize;

public class Serializer {

	public String serialize(Object object) throws IllegalAccessException {
		StringBuilder sb = new StringBuilder();
		serialize(sb, "", object);
		if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
			sb.deleteCharAt(sb.length() - 1);
		}

		return sb.toString();
	}

	//TODO: zabezpieczyc przed stackoverflow jezeli uzyta jest klasa RosJavaLog (teraz zabezpieczylem, ze nie sa serializowane statyczne pola
	public String serialize(StringBuilder sb, String prefix, Object object) throws IllegalAccessException {

		Field[] fields = object.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.getName().startsWith("$jacocoData")) {
				continue;
			}
			if(Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			if (field.getAnnotation(DontSerialize.class) != null) {
				continue;
			}
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			Object value = field.get(object);
			sb.append(prefix);
			sb.append(field.getName());
			if (value == null) {
				sb.append("=null\n");
			} else {
				if (isPrintable(field)) {
					sb.append("=");
					sb.append(value.toString());
					sb.append("\n");
				} else {
					sb.append(":\n");
					serialize(sb, prefix + "  ", value);
				}
			}
		}

		return sb.toString();
	}

	private boolean isPrintable(Field field) {
		Class<?> type = field.getType();
		return type.isAssignableFrom(boolean.class) || type.isAssignableFrom(byte.class)
				|| type.isAssignableFrom(char.class) || type.isAssignableFrom(int.class)
				|| type.isAssignableFrom(short.class) || type.isAssignableFrom(long.class)
				|| type.isAssignableFrom(float.class) || type.isAssignableFrom(double.class)
				|| String.class.isAssignableFrom(type) || Enum.class.isAssignableFrom(type)
				|| Map.class.isAssignableFrom(type) || List.class.isAssignableFrom(type);
	}

}
