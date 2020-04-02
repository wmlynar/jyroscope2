package go.jyroscope.ros.std_msgs;

import com.jyroscope.annotations.Message;

@Message("std_msgs/MultiArrayDimension")
public class MultiArrayDimensionMessage {

	public String label;
	public int size;
	public int stride;

}
