package go.jyroscope.ros.std_msgs;

import com.jyroscope.annotations.Message;
import com.jyroscope.annotations.Primitive;

@Message("std_msgs/Bool")
@Primitive(Boolean.class)
public class BoolMessage {

	public boolean data;

}