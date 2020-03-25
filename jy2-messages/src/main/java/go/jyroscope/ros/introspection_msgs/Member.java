package go.jyroscope.ros.introspection_msgs;

import com.jyroscope.annotations.Hide;
import com.jyroscope.annotations.Message;

@Message("introspection_msgs/Member")
public class Member {
	
	@Hide
	public long time;
    
    public String name;
    public String addr;
    public Node[] nodes;
    
}
