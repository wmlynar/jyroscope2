package go.jyroscope.ros.sensor_msgs;

import com.jyroscope.annotations.Message;

@Message("sensor_msgs/ChannelFloat32")
public class ChannelFloat32 {

	public String name;
	public float[] values;

}
