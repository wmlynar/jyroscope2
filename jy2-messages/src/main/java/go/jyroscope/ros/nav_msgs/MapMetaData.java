package go.jyroscope.ros.nav_msgs;

import java.time.Instant;

import com.jyroscope.annotations.Message;
import com.jyroscope.annotations.Name;

import go.jyroscope.ros.geometry_msgs.Pose;

@Message("nav_msgs/MapMetaData")
public class MapMetaData {

	@Name("map_load_time")
	public Instant mapLoadTime;
	public float resolution;
	public int width;
	public int height;
	public Pose origin;

	public MapMetaData() {
	}

	public MapMetaData(float resolution, int width, int height, double originX, double originY, double originAngle) {
		this.resolution = resolution;
		this.width = width;
		this.height = height;
		origin = new Pose(originX, originY, 0, originAngle);
	}
}