package com.github.jy2.di.internal;

public class InstanceWithName {

	public Object instance;
	public String name;

	public InstanceWithName(Object instance, String name) {
		if (instance == null) {
			throw new IllegalArgumentException("Instance cannot be null");
		}
		this.instance = instance;
		if (name == null) {
			name = "";
		}
		// if (!name.isEmpty() && !name.endsWith("/")) {
		// name = name + "/";
		// }
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + instance.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		InstanceWithName other = (InstanceWithName) obj;
		if (!instance.equals(other.instance))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
