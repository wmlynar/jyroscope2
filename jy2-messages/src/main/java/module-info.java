module aww.jyroscope.messages {
	requires aww.jyroscope.base;
	exports go.jyroscope.ros.geometry_msgs;
	exports go.jyroscope.ros.diagnostic_msgs;
	exports go.jyroscope.ros.introspection_msgs;
	exports go.jyroscope.ros.jy2_msgs;
	exports go.jyroscope.ros.nav_msgs;
	exports go.jyroscope.ros.rosgraph_msgs;
	exports go.jyroscope.ros.sensor_msgs;
	exports go.jyroscope.ros.std_msgs;
	exports go.jyroscope.ros.tf2_msgs;
	exports go.jyroscope.ros.visualization_msgs;
	exports com.github.jy2.messages;
	exports com.github.jy2.utils;

	requires vecmath;
}