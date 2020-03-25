package go.jyroscope.ros.rosgraph_msgs;

import com.jyroscope.annotations.Message;

import go.jyroscope.ros.std_msgs.Header;

@Message("rosgraph_msgs/Log")
public class Log {
	public static final byte DEBUG = 1;
	public static final byte INFO = 2;
	public static final byte WARN = 4;
	public static final byte ERROR = 8;
	public static final byte FATAL = 16;
	
	public Header header;
	public byte level;
	public String name;
	public String msg;
	public String file;
	public String function;
	public int line;
	public String[] topics;
}
