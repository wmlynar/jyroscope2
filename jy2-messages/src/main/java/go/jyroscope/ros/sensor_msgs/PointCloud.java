package go.jyroscope.ros.sensor_msgs;

import com.jyroscope.annotations.Message;

import go.jyroscope.ros.geometry_msgs.Point32;
import go.jyroscope.ros.std_msgs.Header;

@Message("sensor_msgs/PointCloud")
public class PointCloud {
    
    public Header header;
	public Point32[] points;
	public ChannelFloat32[] channels;

}
