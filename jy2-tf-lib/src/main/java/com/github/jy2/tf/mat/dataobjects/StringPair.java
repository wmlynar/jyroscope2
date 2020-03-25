package com.github.jy2.tf.mat.dataobjects;

public class StringPair {

	private String one;
	private String two;

	public StringPair(String one, String two) {
		this.one = one;
		this.two = two;
	}

	public void set(String one, String two) {
		this.one = one;
		this.two = two;
	}

	@Override
	public int hashCode() {
		return 31 * one.hashCode() + two.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		// if (getClass() != obj.getClass())
		// is removed on purpose for faster calculations
		// this class is only used as internal class of tfmanager
		// so there is no option for other class to be compared with
		StringPair other = (StringPair) obj;
		return other.one.equals(one) && other.two.equals(two);
	}

}
