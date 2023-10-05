package go.jyroscope.ros.sensor_msgs;

import java.util.Arrays;

import com.jyroscope.annotations.Message;
import com.jyroscope.annotations.Name;

import go.jyroscope.ros.std_msgs.Header;

@Message("sensor_msgs/LaserScan")
public class LaserScan {
    
    public Header header;
    
    @Name("angle_min") public float angleMin;
    @Name("angle_max") public float angleMax;
    @Name("angle_increment") public float angleIncrement;
    @Name("time_increment") public float timeIncrement;
    @Name("scan_time") public float scanTime;
    @Name("range_min") public float rangeMin;
    @Name("range_max") public float rangeMax;
    
    public float[] ranges;
    public float[] intensities;
    
    public LaserScan() {
    }

    public LaserScan(double time, String frameId, float min, float max, float startAngle, float endAngle,
			float angleIncrement2, float[] ranges2) {
    	header = new Header();
    	header.setSeconds(time);
    	header.frameId = frameId;
    	rangeMin = min;
    	rangeMax = max;
    	angleMin = startAngle;
    	angleMax = endAngle;
    	angleIncrement = angleIncrement2;
    	ranges = ranges2;
	}

    public LaserScan(double time, String frameId, float min, float max, float startAngle, float endAngle,
			float angleIncrement2, float[] ranges2, float[] intensities2) {
    	header = new Header();
    	header.setSeconds(time);
    	header.frameId = frameId;
    	rangeMin = min;
    	rangeMax = max;
    	angleMin = startAngle;
    	angleMax = endAngle;
    	angleIncrement = angleIncrement2;
    	ranges = ranges2;
    	intensities = intensities2;
	}

	public LaserScan(LaserScan scan) {
		header = new Header();
		header.setSeconds(scan.header.toSeconds());
		header.frameId = scan.header.frameId;
		rangeMin = scan.rangeMin;
		rangeMax = scan.rangeMax;
		angleMin = scan.angleMin;
		angleMax = scan.angleMax;
		angleIncrement = scan.angleIncrement;
		ranges = ranges == null ? null : Arrays.copyOf(scan.ranges, scan.ranges.length);
		intensities = intensities == null ? null : Arrays.copyOf(scan.intensities, scan.intensities.length);
	}

	@Override
    public String toString() {
        return "LaserScan{" + "header=" + header + ", angleMin=" + angleMin + ", angleMax=" + angleMax + ", angleIncrement=" + angleIncrement + ", timeIncrement=" + timeIncrement + ", scanTime=" + scanTime + ", rangeMin=" + rangeMin + ", rangeMax=" + rangeMax + ", ranges=" + ranges + ", intensities=" + intensities + '}';
    }
    
}
