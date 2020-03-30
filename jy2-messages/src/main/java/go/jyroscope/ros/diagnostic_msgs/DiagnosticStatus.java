package go.jyroscope.ros.diagnostic_msgs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

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
		values = list.toArray(new KeyValue[0]);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		DiagnosticStatus that = (DiagnosticStatus) o;
		return level == that.level &&
				Objects.equals(name, that.name) &&
				Objects.equals(message, that.message) &&
				Objects.equals(hardwareId, that.hardwareId) &&
				Arrays.equals(values, that.values);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(level, name, message, hardwareId);
		result = 31 * result + Arrays.hashCode(values);
		return result;
	}
}
