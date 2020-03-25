package go.jyroscope.ros.geometry_msgs;

import java.util.Arrays;

import com.jyroscope.annotations.Message;

@Message("geometry_msgs/TwistWithCovariance")
public class TwistWithCovariance {
    
    public Twist twist;
    public double[] covariance;

    @Override
    public String toString() {
        return "TwistWithCovariance{" + "twist=" + twist + ", covariance=" + Arrays.toString(covariance) + '}';
    }
    
}
