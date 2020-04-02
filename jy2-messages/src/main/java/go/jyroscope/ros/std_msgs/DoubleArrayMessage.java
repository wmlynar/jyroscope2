package go.jyroscope.ros.std_msgs;

import com.jyroscope.annotations.Message;

@Message("std_msgs/Float64MultiArray")
public class DoubleArrayMessage {

	public MultiArrayLayoutMessage layout;
	public double[] data;

}
