package go.jyroscope.ros.std_msgs;

import com.jyroscope.annotations.Message;
import com.jyroscope.annotations.Primitive;

@Message("std_msgs/Int64")
@Primitive(Long.class)
public class LongMessage {

	public long data;

}