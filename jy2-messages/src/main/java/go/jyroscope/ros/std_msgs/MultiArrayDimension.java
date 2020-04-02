package go.jyroscope.ros.std_msgs;

import com.jyroscope.annotations.Message;

@Message("std_msgs/MultiArrayDimension")
public class MultiArrayDimension {

	public String label;
	public int size;
	public int stride;

}
