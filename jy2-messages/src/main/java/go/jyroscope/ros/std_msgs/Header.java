package go.jyroscope.ros.std_msgs;

import com.jyroscope.annotations.*;

import java.time.*;
import java.util.Objects;

@Message("std_msgs/Header")
public class Header {

	public int seq;
	public Instant stamp;
	@Name("frame_id")
	public String frameId;

	@Hide
	public double toSeconds() {
		return stamp.getEpochSecond() + stamp.getNano() * 1e-9;
	}

	@Hide
	public void setSeconds(double time) {
		double seconds = Math.floor(time);
		double nanos = (time - seconds) * 1e9;
		stamp = Instant.ofEpochSecond((long) seconds, (long) nanos);
	}

	@Override
	public String toString() {
		return "Header{" + "seq=" + seq + ", stamp=" + stamp + ", frameId=" + frameId + '}';
	}

	public Header() {
	}

	public Header(String frameId, double timeInSeconds) {
		this.frameId = frameId;
		setSeconds(timeInSeconds);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Header header = (Header) o;
		return seq == header.seq &&
				Objects.equals(stamp, header.stamp) &&
				Objects.equals(frameId, header.frameId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(seq, stamp, frameId);
	}
}
