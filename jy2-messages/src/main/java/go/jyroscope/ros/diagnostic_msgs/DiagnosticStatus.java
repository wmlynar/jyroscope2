package go.jyroscope.ros.diagnostic_msgs;

import java.util.ArrayList;

import com.jyroscope.annotations.Hide;
import com.jyroscope.annotations.Message;
import com.jyroscope.annotations.Name;

@Message("diagnostic_msgs/DiagnosticStatus")
public class DiagnosticStatus {

	public static final byte OK = 0;
	public static final byte WARN = 1;
	public static final byte ERROR = 2;
	public static final byte STALE = 3;

	public byte level;
	public String name;
	public String message;
	@Name("hardware_id")
	public String hardwareId;
	public KeyValue[] values;

	@Hide
	public void set(ArrayList<KeyValue> list) {
		values = list.toArray(new KeyValue[list.size()]);
	}
}
