package go.jyroscope.ros.sensor_msgs;

import com.jyroscope.annotations.Message;
import com.jyroscope.annotations.Name;

import go.jyroscope.ros.std_msgs.Header;

@Message("sensor_msgs/CameraInfo")
public class CameraInfo {
    
    public Header header;
    public int height;
    public int width;
    
    @Name("distortion_model")
    public String distortionModel;
    
    public double[] D;
    public double[] K;
    public double[] R;
    public double[] P;
    
    @Name("binning_x")
    public int binningX;
    @Name("binning_y")
    public int binningY;
    
    public RegionOfInterest roi;

    @Override
    public String toString() {
        return "CameraInfo{" + "header=" + header + ", height=" + height + ", width=" + width + ", distortionModel=" + distortionModel + ", D=" + D + ", K=" + K + ", R=" + R + ", P=" + P + ", binningX=" + binningX + ", binningY=" + binningY + ", roi=" + roi + '}';
    }
    
}
