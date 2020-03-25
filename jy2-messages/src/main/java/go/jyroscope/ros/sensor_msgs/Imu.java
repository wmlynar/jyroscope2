package go.jyroscope.ros.sensor_msgs;

import com.jyroscope.annotations.Message;
import com.jyroscope.annotations.Name;

import go.jyroscope.ros.geometry_msgs.Quaternion;
import go.jyroscope.ros.geometry_msgs.Vector3;
import go.jyroscope.ros.std_msgs.Header;

@Message("sensor_msgs/Imu")
public class Imu {
    
    public Header header;
    
    @Name("orientation") public Quaternion orientation;
    @Name("orientation_covariance") public double[] orientationCovariance;
    @Name("angular_velocity") public Vector3 angularVelocity;
    @Name("angular_velocity_covariance") public double[] angularVelocityCovariance;
    @Name("linear_acceleration") public Vector3 linearAcceleration;
    @Name("linear_acceleration_covariance") public double[] linearAccelerationCovariance;

    @Override
    public String toString() {
        return "Imu{" + "header=" + header + ", orientation=" + orientation + ", orientationCovariance=" + orientationCovariance + ", angularVelocity=" + angularVelocity + ", angularVelocityCovariance=" + angularVelocityCovariance + ", linearAcceleration=" + linearAcceleration + ", linearAccelerationCovariance=" + linearAccelerationCovariance + '}';
    }
    
}
