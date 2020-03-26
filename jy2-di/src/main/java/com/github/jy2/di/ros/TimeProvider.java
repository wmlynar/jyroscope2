package com.github.jy2.di.ros;

public class TimeProvider {

	public double now() {
		return ((double) System.currentTimeMillis()) * 0.001;
	}
	
	public Time getCurrentTime() {
		return new Time(System.currentTimeMillis());
	}
}
