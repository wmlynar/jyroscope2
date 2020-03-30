package go.jyroscope.ros.diagnostic_msgs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import com.jyroscope.annotations.Hide;
import com.jyroscope.annotations.Message;

import go.jyroscope.ros.std_msgs.Header;

@Message("diagnostic_msgs/DiagnosticArray")
public class DiagnosticArray {

	public Header header;
	public DiagnosticStatus[] status;

	@Hide
	public void set(ArrayList<DiagnosticStatus> list) {
		status = list.toArray(new DiagnosticStatus[0]);
	}

	public boolean equalsWithoutHeader(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		DiagnosticArray that = (DiagnosticArray) o;
		return Arrays.equals(status, that.status);
	}
}
