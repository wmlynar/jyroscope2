package go.jyroscope.ros.nav_msgs;

import com.jyroscope.annotations.Message;

import go.jyroscope.ros.std_msgs.Header;

@Message("nav_msgs/OccupancyGrid")
public class OccupancyGrid {

	public Header header;
	public MapMetaData info;
	public byte[] data;

	public OccupancyGrid() {
	}

	public OccupancyGrid(double time, String frameId, int width, int height, double resolution, double x, double y, double a, byte[] data) {
		header = new Header(frameId, time);
		info = new MapMetaData((float) resolution, width, height, x, y, a);
		this.data = data;
	}
}