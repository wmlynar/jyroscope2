package go.jyroscope.ros.geometry_msgs;

import com.jyroscope.annotations.Message;

import go.jyroscope.ros.std_msgs.Header;

@Message("geometry_msgs/PointStamped")
public class PointStamped {

	public Header header;
	public Point point;
    
}
