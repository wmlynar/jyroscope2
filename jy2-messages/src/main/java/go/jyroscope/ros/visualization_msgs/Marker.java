package go.jyroscope.ros.visualization_msgs;

import java.time.Duration;

import com.jyroscope.annotations.Message;
import com.jyroscope.annotations.Name;

import go.jyroscope.ros.geometry_msgs.Point;
import go.jyroscope.ros.geometry_msgs.Pose;
import go.jyroscope.ros.geometry_msgs.Vector3;
import go.jyroscope.ros.std_msgs.ColorRGBA;
import go.jyroscope.ros.std_msgs.Header;

@Message("visualization_msgs/Marker")
public class Marker {

	public static final byte ARROW = 0;
	public static final byte CUBE = 1;
	public static final byte SPHERE = 2;
	public static final byte CYLINDER = 3;
	public static final byte LINE_STRIP = 4;
	public static final byte LINE_LIST = 5;
	public static final byte CUBE_LIST = 6;
	public static final byte SPHERE_LIST = 7;
	public static final byte POINTS = 8;
	public static final byte TEXT_VIEW_FACING = 9;
	public static final byte MESH_RESOURCE = 10;
	public static final byte TRIANGLE_LIST = 11;

	public static final byte ADD = 0;
	public static final byte MODIFY = 0;
	public static final byte DELETE = 2;
	public static final byte DELETEALL = 3;

	public Header header;
	public String ns;
	public int id;
	public int type;
	public int action;
	public Pose pose;
	public Vector3 scale;
	public ColorRGBA color;
	public Duration lifetime;
	@Name("frame_locked")
	public boolean frameLocked;

	public Point[] points;
	public ColorRGBA[] colors;

	public String text;

	@Name("mesh_resource")
	public String meshResource;
	@Name("mesh_use_embedded_materials")
	public boolean meshUseEmbeddedMaterials;

}
