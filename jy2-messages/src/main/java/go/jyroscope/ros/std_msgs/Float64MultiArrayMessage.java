package go.jyroscope.ros.std_msgs;

import com.jyroscope.annotations.Message;

@Message("std_msgs/Float64MultiArray")
public class Float64MultiArrayMessage {

	public MultiArrayLayoutMessage layout;
	public double[] data;

	public static Float64MultiArrayMessage createArrayWithSize(int size) {
		Float64MultiArrayMessage reasult = new Float64MultiArrayMessage();
		reasult.layout = new MultiArrayLayoutMessage();
		reasult.layout.dim = new MultiArrayDimensionMessage[1];
		reasult.layout.dim[0] = new MultiArrayDimensionMessage();
		reasult.layout.dim[0].size = size;
		reasult.data = new double[size];
		return reasult;
	}
}