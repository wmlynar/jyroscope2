package com.jyroscope.ros.tcpros;

import com.jyroscope.ros.*;
import com.jyroscope.ros.types.*;
import java.io.*;
import java.nio.*;
import java.util.*;


public class TCPROSHeader {
    
    private HashMap<String,String> fields;
    
    public TCPROSHeader() {
        fields = new HashMap<String, String>();
    }
    
    public void putHeader(String key, String value) {
        fields.put(key, value);
    }
    
    public String getHeader(String key) {
        return fields.get(key);
    }
    
    public void render(RosMessage buffer) throws IOException {
        for (Map.Entry<String, String> entry : fields.entrySet())
            buffer.putString(entry.getKey() + "=" + entry.getValue());
        buffer.flip();
    }
    
    public void parse(RosMessage buffer) throws IOException {
        while (buffer.position() < buffer.limit()) {
            String line = buffer.getString();
            int equals = line.indexOf('=');
            fields.put(line.substring(0, equals), line.substring(equals + 1));
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        sb.append("TCPROSHeader[");
        for (HashMap.Entry<String,String> entry : fields.entrySet()) {
            if (first)
                first = false;
            else
                sb.append(",");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        sb.append("]");
        return sb.toString();
    }

    public int count() {
        return fields.size();
    }
    
}
