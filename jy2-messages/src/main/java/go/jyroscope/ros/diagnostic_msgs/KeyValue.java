package go.jyroscope.ros.diagnostic_msgs;

import com.jyroscope.annotations.Message;

@Message("diagnostic_msgs/KeyValue")
public class KeyValue {
    
    public String key;
    public String value;
    
	public KeyValue(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public KeyValue() {
		this("", "");
	}
	
}
