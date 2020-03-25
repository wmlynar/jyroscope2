package go.jyroscope.ros.geometry_msgs;

import javax.vecmath.Matrix4d;

import com.github.jy2.messages.Pose2d;
import com.github.jy2.utils.MessagesQuaternionUtils;
import com.jyroscope.annotations.Hide;
import com.jyroscope.annotations.Message;

import go.jyroscope.ros.std_msgs.Header;

@Message("geometry_msgs/PoseWithCovarianceStamped")
public class PoseWithCovarianceStamped {

    public Header header;
    public PoseWithCovariance pose;
    
    public PoseWithCovarianceStamped() {
    }

    public PoseWithCovarianceStamped(double seconds, String frameId, Pose2d pose2d) {
    	this.header = new Header();
    	this.header.setSeconds(seconds);
    	this.header.frameId = frameId;
		this.pose.pose.position.x = pose2d.x;
		this.pose.pose.position.y = pose2d.y;
		this.pose.pose.position.z = 0;
		MessagesQuaternionUtils.yawEulerAngleToQuaternion(pose2d.a, this.pose.pose.orientation);
	}

	@Hide
	public PoseWithCovarianceStamped set(Matrix4d t) {
		this.pose.pose.position.x = t.m03;
		this.pose.pose.position.y = t.m13;
		this.pose.pose.position.z = 0;
		MessagesQuaternionUtils.yawEulerAngleToQuaternion(-Math.atan2(t.m01, t.m00), this.pose.pose.orientation);
		return this;
	}

    @Hide
	public void get(Matrix4d matrix) {
		matrix.rotZ(MessagesQuaternionUtils.quaternionToYawEulerAngle(pose.pose.orientation));
		matrix.m03 = pose.pose.position.x;
		matrix.m13 = pose.pose.position.y;
	}
    
}
