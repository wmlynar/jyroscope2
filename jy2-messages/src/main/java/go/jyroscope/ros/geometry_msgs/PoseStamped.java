package go.jyroscope.ros.geometry_msgs;

import com.jyroscope.annotations.Message;

import go.jyroscope.ros.std_msgs.Header;

@Message("geometry_msgs/PoseStamped")
public class PoseStamped {

    public Header header;
    public Pose pose;

    
}
