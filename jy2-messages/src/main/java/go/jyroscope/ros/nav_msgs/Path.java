package go.jyroscope.ros.nav_msgs;

import com.jyroscope.annotations.Message;
import go.jyroscope.ros.geometry_msgs.PoseStamped;
import go.jyroscope.ros.std_msgs.Header;

@Message("nav_msgs/Path")
public class Path {
	public Header header;
	public PoseStamped[] poses;
}
