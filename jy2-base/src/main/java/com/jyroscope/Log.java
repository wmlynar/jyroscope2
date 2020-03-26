package com.jyroscope;

import java.util.logging.*;

public class Log {

    private static final String LOG_NAME = "com.jyroscope";
    private static final Logger LOG = Logger.getLogger(LOG_NAME);
    
    public static void finest(Object origin, String message) {
        if (origin instanceof Class)
            LOG.log(Level.FINEST, "In " + String.valueOf(origin) + ": " + message);
        else
            LOG.log(Level.FINEST, origin.getClass().getSimpleName() + " " + String.valueOf(origin) + ": " + message);
    }
    
    public static void warn(Object origin, String message) {
        if (origin instanceof Class)
            LOG.log(Level.WARNING, "In " + String.valueOf(origin) + ": " + message);
        else
            LOG.log(Level.WARNING, origin.getClass().getSimpleName() + " " + String.valueOf(origin) + ": " + message);
    }
    
    public static void msg(Object origin, String message) {
        if (origin instanceof Class)
            LOG.log(Level.INFO, "In " + String.valueOf(origin) + ": " + message);
        else
            LOG.log(Level.INFO, origin.getClass().getSimpleName() + " " + String.valueOf(origin) + ": " + message);
    }
    
    public static void exception(Object origin, Throwable cause, String message) {
        if (origin instanceof Class)
            LOG.log(Level.SEVERE, "In " + String.valueOf(origin) + ": " + message, cause);
        else
            LOG.log(Level.SEVERE, origin.getClass().getSimpleName() + " " + String.valueOf(origin) + ": " + message, cause);
    }
    
}
