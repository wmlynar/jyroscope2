package go.jyroscope.ros.geometry_msgs;

import javax.vecmath.Quat4d;

import com.jyroscope.annotations.Hide;
import com.jyroscope.annotations.Message;

@Message("geometry_msgs/Quaternion")
public class Quaternion {

	public double x;
	public double y;
	public double z;
	public double w;

	@Override
	public String toString() {
		return "Quaternion{" + "x=" + x + ", y=" + y + ", z=" + z + ", w=" + w + '}';
	}

	public Quaternion() {
	}

	public Quaternion(double x, double y, double z, double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	@Hide
	public void get(Quat4d q) {
		q.x = x;
		q.y = y;
		q.z = z;
		q.w = w;
	}

	@Hide
	public void set(Quat4d q) {
		x = q.x;
		y = q.y;
		z = q.z;
		w = q.w;
	}

	@Hide
	public void zero() {
		x = 0;
		y = 0;
		z = 0;
		w = 0;
	}
	
	@Hide
	public Quat4d asQuat4d() {
		Quat4d quat = new Quat4d();
		get(quat);
		return quat;
	}

	public void setYawEulerAngle(double yaw) {
		x = 0;
		y = 0;
		w = Math.cos(yaw * 0.5);
		z = Math.sin(yaw * 0.5);
	}

	public double getYawEulerAngle() {
		double tempA = 2 * w * z;
		double tempB = 1 - 2 * z * z;
		return Math.atan2(tempA, tempB);
	}

}
