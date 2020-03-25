package go.jyroscope.ros.diagnostic_msgs;

import java.util.ArrayList;

import com.jyroscope.annotations.Hide;
import com.jyroscope.annotations.Message;

import go.jyroscope.ros.std_msgs.Header;

@Message("diagnostic_msgs/DiagnosticArray")
public class DiagnosticArray {

	public Header header;
	public DiagnosticStatus[] status;

	@Hide
	public void set(ArrayList<DiagnosticStatus> list) {
		status = list.toArray(new DiagnosticStatus[list.size()]);
	}

}
