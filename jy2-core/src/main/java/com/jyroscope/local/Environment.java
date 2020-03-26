package com.jyroscope.local;

import java.io.*;
import java.util.*;
import java.util.stream.*;

public class Environment {

    public static String getValue(String name) {
        String value;
        if (name.startsWith("$")) {
            name = name.substring(1);
            value = System.getenv(name);
            if (value == null)
                throw new IllegalArgumentException("Could not find system environment variable " + name);
        } else
            value = name;
        return value;
    }
    
    public static List<String> getPaths(String name) {
        String value = getValue(name);
        String[] paths = value.split(":|;|" + File.pathSeparator);
        return Arrays.stream(paths)
                .map(s -> s.trim())
                .filter(s -> s.length() > 0)
                .collect(Collectors.toList());
    }
    
}
