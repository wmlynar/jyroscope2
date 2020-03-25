package go.jyroscope.ros.std_msgs;

import com.jyroscope.annotations.Message;

@Message("std_msgs/ColorRGBA")
public class ColorRGBA {

	public float r;
	public float g;
	public float b;
	public float a;

	public ColorRGBA(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	public ColorRGBA() {
	}
}