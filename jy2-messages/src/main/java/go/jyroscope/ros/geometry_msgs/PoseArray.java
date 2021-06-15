package go.jyroscope.ros.geometry_msgs;

import com.jyroscope.annotations.Message;

import go.jyroscope.ros.std_msgs.Header;

@Message("geometry_msgs/PoseArray")
public class PoseArray {

	public Header header;
	public Pose[] poses;

}
