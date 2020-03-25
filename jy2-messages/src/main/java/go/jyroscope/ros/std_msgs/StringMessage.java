package go.jyroscope.ros.std_msgs;

import com.jyroscope.annotations.Message;
import com.jyroscope.annotations.Primitive;

@Message("std_msgs/String")
@Primitive(String.class)
public class StringMessage {

    public String data;

}