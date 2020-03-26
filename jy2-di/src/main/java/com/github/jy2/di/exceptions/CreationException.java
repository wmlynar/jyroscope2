package com.github.jy2.di.exceptions;

public class CreationException extends Exception {

	private static final long serialVersionUID = 1L;

    public CreationException(String message) {
        super(message);
    }
    
    public CreationException(Throwable cause) {
        super(cause);
    }
    
    public CreationException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
