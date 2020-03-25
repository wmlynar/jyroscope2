package go.jyroscope.ros.geometry_msgs;

import com.jyroscope.annotations.Message;

@Message("geometry_msgs/Point")
public class Point {
    
    public double x;
    public double y;
    public double z;

    public Point() {
    }

    public Point(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return "Point{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
    }
    
}
