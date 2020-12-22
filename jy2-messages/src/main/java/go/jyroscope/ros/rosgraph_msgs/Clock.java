package go.jyroscope.ros.rosgraph_msgs;

import java.time.Instant;

import com.jyroscope.annotations.Message;

@Message("rosgraph_msgs/Clock")
public class Clock {
	public Instant clock;
}
