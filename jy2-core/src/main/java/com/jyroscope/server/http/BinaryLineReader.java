package com.jyroscope.server.http;

import java.io.*;

public class BinaryLineReader {
    
    private static final int CR = 13;
    private static final int LF = 10;
    
    private StringBuffer sb;
    private InputStream is;
    
    public BinaryLineReader(InputStream is) {
        this.is = is;
        sb = new StringBuffer();
    }
    
    public String readLine() throws IOException {
        if (sb == null)
            return null;
        int next;
        boolean seenCR = false;
        line: {
            while ((next = is.read()) != -1) {
                if (seenCR) {
                    if (next == LF)
                        break line; // Found EOL
                    else {
                        sb.append((char)CR).append((char)next);
                        seenCR = false;
                    }
                } else {
                    if (next == CR)
                        seenCR = true;
                    else
                        sb.append((char)next);
                }
            }
            
            // EOF fall-through
            String result = sb.toString();
            sb = null;
            return result;
        }
        // EOL
        String result = sb.toString();
        sb = new StringBuffer();
        return result;
    }
    
    public String readTo(int length) throws IOException {
        if (sb == null)
            return null;
        
        if (sb.capacity() - sb.length() < length)
            sb.ensureCapacity(length + sb.length());
        
        int next = 0;
        while (length > 0 && (next = is.read()) != -1) {
            sb.append((char)next);
            length--;
        }
        
        String result = sb.toString();
        if (next == -1)
            sb = null;
        else
            sb = new StringBuffer();
        return result;
    }
    
    public String readAll() throws IOException {
        if (sb == null)
            return null;
        
        int next;
        while ((next = is.read()) != -1)
            sb.append((char)next);
        
        String result = sb.toString();
        sb = null;
        return result;
    }

}
