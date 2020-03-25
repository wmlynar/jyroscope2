package go.jyroscope.ros.sensor_msgs;

import com.jyroscope.annotations.Message;
import com.jyroscope.annotations.Name;

@Message("sensor_msgs/RegionOfInterest")
public class RegionOfInterest {
    
    @Name("x_offset")
    public int xOffset;
    
    @Name("y_offset")
    public int yOffset;
    
    public int height;
    public int width;
    
    @Name("do_rectify")
    public boolean doRectify;
    
}
