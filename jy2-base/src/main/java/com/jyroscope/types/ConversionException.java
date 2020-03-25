package com.jyroscope.types;

public class ConversionException extends Exception {
    
    public ConversionException(String message) {
        super(message);
    }
    
    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ConversionException(Throwable cause) {
        super(cause);
    }

}
