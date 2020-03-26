package com.jyroscope.server.http;

import java.io.*;
import java.net.*;
import java.util.*;

public class HTTPAction {
    
    private static byte[] CRLF = {13, 10};
    
    private String requestMethod;
    private String requestFirst;
    private String requestPath;
    private HashMap<String, String> requestHeaders;
    private String requestBody;
    private SocketAddress clientAddress;
    private HashMap<String, String> responseHeaders;
    private HashMap<String, String> canonicalNames;
    private byte[] responseBody;
    private int responseCode = 200;
    private String responseMessage = "OK";
    
    public HTTPAction(SocketAddress clientAddress) {
        this.clientAddress = clientAddress;
    }
    
    public String getMethod() throws HTTPException {
        return requestMethod;
    }
    
    public String getRequestBody() {
        return requestBody;
    }
    
    public String getHeader(String key) {
        return requestHeaders.get(key.toLowerCase());
    }
    
    public SocketAddress getClient() {
        return clientAddress;
    }
    
    public void setContentType(String value) {
        setResponseHeader("Content-Type", value);
    }
    
    public void setResponseHeader(String key, String value) {
        if (responseHeaders == null)
            responseHeaders = new HashMap<String, String>();
        responseHeaders.put(getCanonicalName(key), value);
    }
    
    private String getCanonicalName(String name) {
        if (canonicalNames == null)
            canonicalNames = new HashMap<String, String>();
        String lowerCase = name.toLowerCase();
        String result = canonicalNames.get(lowerCase);
        if (result == null)
            canonicalNames.put(lowerCase, result = name);
        return result;
    }
    
    public void setResponseHeaders(HashMap<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }
    
    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody.getBytes();
    }
    
    public void setResponseBody(byte[] responseBody) {
        this.responseBody = responseBody;
    }
    
    public void setResponseCode(int code, String message) {
        this.responseCode = code;
        this.responseMessage = message;
    }
    
    public String toString() {
        if (responseHeaders == null)
            return requestMethod + " " + requestPath + "[" + requestHeaders + requestBody + "]";
        else
            return requestMethod + " " + requestPath + "[" + requestHeaders + requestBody + "] -> " + responseCode + "[" + responseHeaders + new String(responseBody) + "]";  
    }
    
    public void parseIn(InputStream is) throws IOException, HTTPException {
        BinaryLineReader blr = new BinaryLineReader(is);
        String currentHeader = null;
        requestHeaders = new HashMap<String, String>();
        
        // Read the first line, the request line
        requestFirst = blr.readLine();
        int space1 = requestFirst.indexOf(' ');
        if (space1 == -1)
            throw new HTTPException(400, "Bad request line");
        requestMethod = requestFirst.substring(0, space1).toUpperCase();
        int space2 = requestFirst.indexOf(' ', space1 + 1);
        if (space2 == -1)
            throw new HTTPException(400, "Bad request line");
        requestPath = requestFirst.substring(space1 + 1, space2);
        
        // Next, read the headers
        String line = blr.readLine();
        while (line != null && line.length() != 0) {
            if (line.startsWith(" ") || line.startsWith("\t"))
                requestHeaders.put(currentHeader, requestHeaders.get(currentHeader) + line);
            else {
                int pos = line.indexOf(':');
                if (pos > -1) {
                    currentHeader = line.substring(0, pos).toLowerCase();
                    String value = line.substring(pos + 1);
                    if (requestHeaders.containsKey(currentHeader))
                        requestHeaders.put(currentHeader, requestHeaders.get(currentHeader) + "," + value);
                    else
                        requestHeaders.put(currentHeader, value);
                } else {
                    throw new HTTPException(400, "Misformed header");
                }
            }
            line = blr.readLine();
        }
        if (line == null)
            throw new HTTPException(400, "Unexpected end of request");
        
        // Finish with the body!
        // TODO support transfer-coding per http/1.1 spec
        if (requestHeaders.containsKey("content-length")) {
            int length = Integer.parseInt(requestHeaders.get("content-length").trim());
            requestBody = blr.readTo(length);
        }
    }
    
    public void writeOut(OutputStream os) throws IOException {
        os = new BufferedOutputStream(os);
        os.write(("HTTP/1.0 " + responseCode + " " + responseMessage).getBytes());
        os.write(CRLF);
        boolean contentLength = false;
        for (Map.Entry<String, String> entry : responseHeaders.entrySet()) {
            String key = entry.getKey();
            if (key.equalsIgnoreCase("Content-Length"))
                contentLength = true;
            os.write((key + ": " + entry.getValue()).getBytes());
            os.write(CRLF);
        }
        if (!contentLength) {
            os.write(("Content-Length: " + responseBody.length).getBytes());
            os.write(CRLF);
        }
        os.write(CRLF);
        if (responseBody != null && responseBody.length != 0) {
            os.write(responseBody);
        }
        os.close();
    }

}
