package go.jyroscope.ros.geometry_msgs;

import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import com.jyroscope.annotations.Hide;
import com.jyroscope.annotations.Message;

@Message("geometry_msgs/Transform")
public class Transform {

    public Vector3 translation = new Vector3();
    public Quaternion rotation = new Quaternion();;
    
    public Transform() {
    }
    
	public Transform(Matrix4d m) {
		set(m);
	}

	@Hide
	public void get(Matrix4d m) {
		Quat4d q = new Quat4d();
		Vector3d v = new Vector3d();
		rotation.get(q);
		translation.get(v);
		m.set(q,v,1);
	}
	
	@Hide
	public void set(Matrix4d matrix) {
		Quat4d q = new Quat4d();
		Vector3d v = new Vector3d();
		// WARNING! always do quat.get(matrix)
		// NEVER do mat.set(quat) as it may return NaN when quat is not normalized
		matrix.get(v);
		q.set(matrix);
		// VERY IMPORTANT! without it the multiplication of matrices accumulates errors
		q.normalize();
		translation.set(v);
		rotation.set(q);
	}
    
	@Hide
	public void setIdentity() {
		translation.zero();
		rotation.zero();
	}

	@Hide
	public Matrix4d asMatrix() {
		Matrix4d mat = new Matrix4d();
		get(mat);
		return mat;
	}
}
