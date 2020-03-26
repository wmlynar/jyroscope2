package com.jyroscope.util;

public class Id {

    private static int idGenerator;

	public static synchronized void reset() {
		idGenerator = 0;
	}

    public static synchronized String generate() {
            return "x" + (++idGenerator);
    }
	
    public static String generate(String... parts) {
        StringBuilder result = new StringBuilder();
        
        for (String part : parts) {
            if (part.contains("."))
                part = part.substring(part.lastIndexOf(".") + 1);
            for (int i=0; i<part.length(); i++) {
                char c = part.charAt(i);
                if (Character.isJavaIdentifierPart(c))
                    result.append(c);
            }
            result.append('_');
        }
        return result.toString() + generate();
    }
    
}
