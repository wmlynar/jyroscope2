package com.github.jy2.di.ros;

@Deprecated
public class Time {

	long time;

	public Time(long time) {
		this.time = time;
	}

	public Time(double time) {
		this.time = (long) (time * 1000.0);
	}

	public double toSeconds() {
		return ((double) time) * 0.001;
	}

}
