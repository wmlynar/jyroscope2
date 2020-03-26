package go.jyroscope.ros.std_msgs;

import com.jyroscope.annotations.Message;
import com.jyroscope.annotations.Primitive;

@Message("std_msgs/Int32")
@Primitive(Integer.class)
public class IntMessage {

	public int data;

}