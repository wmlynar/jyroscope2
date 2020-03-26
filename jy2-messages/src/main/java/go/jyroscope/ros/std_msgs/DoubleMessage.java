package go.jyroscope.ros.std_msgs;

import com.jyroscope.annotations.Message;
import com.jyroscope.annotations.Primitive;

@Message("std_msgs/Float64")
@Primitive(Double.class)
public class DoubleMessage {

	public double data;

}