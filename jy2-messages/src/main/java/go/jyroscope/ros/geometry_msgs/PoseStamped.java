package go.jyroscope.ros.geometry_msgs;

import com.github.jy2.messages.Pose2d;
import com.jyroscope.annotations.Message;

import go.jyroscope.ros.std_msgs.Header;

@Message("geometry_msgs/PoseStamped")
public class PoseStamped {

	public Header header;
	public Pose pose;

	public PoseStamped() {
	}

	public PoseStamped(double seconds, String frameId, Pose2d pose2d) {
		this.header = new Header();
		this.header.setSeconds(seconds);
		this.header.frameId = frameId;
		this.pose = new Pose(pose2d.x, pose2d.y, 0, pose2d.a);
	}

}
