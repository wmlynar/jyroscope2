package go.jyroscope.ros.diagnostic_msgs;

import com.jyroscope.annotations.Message;

import java.util.Objects;

@Message("diagnostic_msgs/KeyValue")
public class KeyValue {
    
    public String key;
    public String value;
    
	public KeyValue() {
		this("", "");
	}
	
	public KeyValue(String key, String value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		KeyValue keyValue = (KeyValue) o;
		return Objects.equals(key, keyValue.key) &&
				Objects.equals(value, keyValue.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}
}
