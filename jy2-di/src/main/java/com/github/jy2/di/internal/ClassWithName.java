package com.github.jy2.di.internal;

public class ClassWithName {

	public Class<?> type;
	public String name;

	public ClassWithName(Class<?> type, String name) {
		if (type == null) {
			throw new IllegalArgumentException("Type cannot be null");
		}
		this.type = type;
		if (name == null) {
			name = "";
		}
		this.name = name;
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + name.hashCode();
		result = 31 * result + type.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		ClassWithName other = (ClassWithName) obj;
		return type.equals(other.type) && name.equals(other.name);
	}

}
