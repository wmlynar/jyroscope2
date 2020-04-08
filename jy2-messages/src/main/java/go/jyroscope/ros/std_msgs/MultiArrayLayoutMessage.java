package go.jyroscope.ros.std_msgs;

import com.jyroscope.annotations.Message;

@Message("std_msgs/MultiArrayLayout")
public class MultiArrayLayoutMessage {

	public MultiArrayDimensionMessage[] dim;
	public int data_offset;

}
