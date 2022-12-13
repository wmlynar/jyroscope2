package go.jyroscope.ros.geometry_msgs;

import com.jyroscope.annotations.Message;

@Message("geometry_msgs/Pose")
public class Pose {

	public Point position;
	public Quaternion orientation;

	@Override
	public String toString() {
		return "Pose{" + "position=" + position + ", orientation=" + orientation + '}';
	}

	public Pose() {
		position = new Point();
		orientation = new Quaternion();
	}

	public Pose(double x, double y, double z, double a) {
		position = new Point(x, y, z);
		orientation = new Quaternion();
		orientation.setYawEulerAngle(a);
	}
}
