package go.jyroscope.ros.introspection_msgs;

import com.jyroscope.annotations.Message;

@Message("introspection_msgs/Node")
public class Node {
    
    public String name;
    public String[] publishers;
    public String[] subscribers;
    
}
