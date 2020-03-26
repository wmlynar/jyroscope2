package go.jyroscope.ros.std_msgs;

import java.time.Instant;

import com.jyroscope.annotations.Message;
import com.jyroscope.annotations.Primitive;

@Message("std_msgs/Time")
@Primitive(Instant.class)
public class TimeMessage {

	public Instant data;

}