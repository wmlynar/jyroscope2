package com.jyroscope.server.http;

public interface HTTPService {
    
    public void process(HTTPAction action) throws HTTPException;

}
