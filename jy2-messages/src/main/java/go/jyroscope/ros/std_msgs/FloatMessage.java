package go.jyroscope.ros.std_msgs;

import com.jyroscope.annotations.Message;
import com.jyroscope.annotations.Primitive;

@Message("std_msgs/Float32")
@Primitive(Float.class)
public class FloatMessage {

	public float data;

}