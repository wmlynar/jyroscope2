package go.jyroscope.ros.geometry_msgs;

import java.util.Arrays;

import com.jyroscope.annotations.Message;

@Message("geometry_msgs/PoseWithCovariance")
public class PoseWithCovariance {
    
    public Pose pose;
    public double[] covariance;

    @Override
    public String toString() {
        return "PoseWithCovariance{" + "pose=" + pose + ", covariance=" + Arrays.toString(covariance) + '}';
    }
    
}
