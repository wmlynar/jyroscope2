package go.jyroscope.ros.std_msgs;

import com.jyroscope.annotations.Message;

@Message("std_msgs/Float64MultiArray")
public class DoubleArray {

	public MultiArrayLayout layout;
	public double[] data;

}
