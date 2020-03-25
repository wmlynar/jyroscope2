package go.jyroscope.ros.geometry_msgs;

import com.jyroscope.annotations.Message;

@Message("geometry_msgs/Point32")
public class Point32 {
    
	public float x;
	public float y;
	public float z;

    @Override
    public String toString() {
        return "Point{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
    }
    
}
