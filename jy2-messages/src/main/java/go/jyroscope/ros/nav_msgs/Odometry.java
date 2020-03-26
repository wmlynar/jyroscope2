package go.jyroscope.ros.nav_msgs;

import com.jyroscope.annotations.Message;
import com.jyroscope.annotations.Name;

import go.jyroscope.ros.geometry_msgs.PoseWithCovariance;
import go.jyroscope.ros.geometry_msgs.TwistWithCovariance;
import go.jyroscope.ros.std_msgs.Header;

@Message("nav_msgs/Odometry")
public class Odometry {
    
    public Header header;
    
    @Name("child_frame_id")
    public String childFrameId;
    
	public PoseWithCovariance pose;
    public TwistWithCovariance twist;

    @Override
    public String toString() {
        return "Odometry{" + "header=" + header + ", childFrameId=" + childFrameId + ", pose=" + pose + ", twist=" + twist + '}';
    }
    
}