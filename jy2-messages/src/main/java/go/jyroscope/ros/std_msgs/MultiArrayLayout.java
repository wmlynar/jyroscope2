package go.jyroscope.ros.std_msgs;

import com.jyroscope.annotations.Message;

@Message("std_msgs/MultiArrayLayout")
public class MultiArrayLayout {

	public MultiArrayDimension[] dim;
	public int data_offset;

}
