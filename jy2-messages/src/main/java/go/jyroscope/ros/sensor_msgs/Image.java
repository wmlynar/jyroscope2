package go.jyroscope.ros.sensor_msgs;

import com.jyroscope.annotations.Message;
import com.jyroscope.annotations.Name;

import go.jyroscope.ros.std_msgs.Header;

@Message("sensor_msgs/Image")
public class Image {
    
    public Header header;
    public int height;
    public int width;
    public String encoding;
    @Name("is_bigendian")
	public boolean isBigEndian;
    public int step;
	public byte[] data;

    @Override
    public String toString() {
        return "Image{" + "header=" + header + ", height=" + height + ", width=" + width + ", encoding=" + encoding + ", isBigEndian=" + isBigEndian + ", step=" + step + ", data=" + data + '}';
    }
    
}
