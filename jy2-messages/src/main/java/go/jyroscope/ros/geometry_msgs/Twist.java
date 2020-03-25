package go.jyroscope.ros.geometry_msgs;

import com.jyroscope.annotations.Message;

@Message("geometry_msgs/Twist")
public class Twist {
    
    public Vector3 linear;
    public Vector3 angular;

    public Twist() {
        // no-arg constructor
    }
    
    public Twist(Vector3 linear, Vector3 angular) {
        this.linear = linear;
        this.angular = angular;
    }
    
    @Override
    public String toString() {
        return "Twist{" + "linear=" + linear + ", angular=" + angular + '}';
    }
    
}
