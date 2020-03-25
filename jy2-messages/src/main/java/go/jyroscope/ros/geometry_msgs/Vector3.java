package go.jyroscope.ros.geometry_msgs;

import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import com.jyroscope.annotations.Hide;
import com.jyroscope.annotations.Message;

@Message("geometry_msgs/Vector3")
public class Vector3 {

	public double x;
	public double y;
	public double z;

	public Vector3() {
		// no-arg constructor
	}

	public Vector3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Hide
	public void get(Vector3d v) {
		v.x = x;
		v.y = y;
		v.z = z;
	}

	@Hide
	public void set(Vector3d v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}

	@Hide
	public void set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Hide
	public void zero() {
		x = 0;
		y = 0;
		z = 0;
	}

	@Hide
	public Vector3d asVector3d() {
		Vector3d vec = new Vector3d();
		get(vec);
		return vec;
	}
	
	@Override
	public String toString() {
		return "Vector3{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
	}

}
