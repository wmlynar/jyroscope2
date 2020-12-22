package com.github.jy2.di.ros;

import java.time.Instant;

import com.github.jy2.di.annotations.Parameter;
import com.github.jy2.di.annotations.Subscribe;

import go.jyroscope.ros.rosgraph_msgs.Clock;

public class TimeProvider {
	
    @Parameter("/use_sim_time")
    boolean useSimTime = false;
	private Instant clock;

	public double now() {
		if(useSimTime && clock!=null) {
			return clock.getEpochSecond() + clock.getNano() * 1e-9;
		}
		return ((double) System.currentTimeMillis()) * 0.001;
	}
	
	public Instant instant() {
		if(useSimTime && clock!=null) {
			return clock;
		}
		return Instant.now();
	}
	
	@Deprecated
	public Time getCurrentTime() {
		return new Time(System.currentTimeMillis());
	}
	
    @Subscribe("/clock")
    public void onClock(Clock message) {
        clock = message.clock;
    }
}
