package go.jyroscope.ros.tf2_msgs;

import java.util.ArrayList;

import com.jyroscope.annotations.Message;

import go.jyroscope.ros.geometry_msgs.TransformStamped;

@Message("tf2_msgs/TFMessage")
public class TFMessage {
	
	public TransformStamped[] transforms;

	public void set(ArrayList<TransformStamped> list) {
		transforms = list.toArray(new TransformStamped[list.size()]);
	}

	public void set(TransformStamped transform) {
		transforms = new TransformStamped[1];
		transforms[0] = transform;
	}
	
}
