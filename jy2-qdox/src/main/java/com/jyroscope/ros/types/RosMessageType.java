package com.jyroscope.ros.types;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jyroscope.FormatException;
import com.jyroscope.util.Hex;


public class RosMessageType implements RosType {
    
    private String name;
    private LinkedHashMap<String, RosType> fields;
    private LinkedHashMap<String, String> constants;
    
    RosMessageType(String name) {
        this.name = name;
        fields = new LinkedHashMap<>();
        constants = new LinkedHashMap<>();
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public int getSize() {
        int totalSize = 0;
        for (Map.Entry<String,RosType> field : fields.entrySet()) {
            int next = field.getValue().getSize();
            if (next == -1)
                return -1;
            else
                totalSize += next;
        }
        return totalSize;
    }
    
    @Override
    public int getMinimumSize() {
        int totalSize = 0;
        for (Map.Entry<String,RosType> field : fields.entrySet())
            totalSize += field.getValue().getMinimumSize();
        return totalSize;
    }
    
    @Override
    public String getHash() {
        StringBuilder text = new StringBuilder();
        boolean isFirst = true;
        for (Map.Entry<String, String> entry : constants.entrySet()) {
            if (isFirst)
                isFirst = false;
            else
                text.append("\n");
            String key = entry.getKey();
            text.append(fields.get(key).getName()).append(" ").append(key).append("=").append(entry.getValue());
        }
        for (Map.Entry<String, RosType> entry : fields.entrySet()) {
            String key = entry.getKey();
            if (!constants.containsKey(key)) {
                if (isFirst)
                    isFirst = false;
                else
                    text.append("\n");
                text.append(entry.getValue().getHash()).append(" ").append(key);
            }
        }
        try {
            MessageDigest hasher = MessageDigest.getInstance("MD5");
            byte[] hash = hasher.digest(text.toString().getBytes("UTF-8"));
            return Hex.toHex(hash);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            // this should never happen
            throw new RuntimeException(e);
        }
    }
    
    public void parseMsgFile(Reader reader) throws IOException, FormatException {
        Pattern pattern = Pattern.compile(
                "([^\\[ \t\n\f\r]*)\\s*" +      // 1 = type
                "(\\[\\s*([0-9]+)?\\s*\\])?" +  // 2 = brackets, 3 = number
                "\\s+" +
                "([^ #]+)" +                     // 4 = name
                "\\s*"
        );
        
        BufferedReader br = new BufferedReader(reader);
        String line;
        while ((line = br.readLine()) != null) {
            // First handle constant values
            // Curiously, comments are included in the values of constants when it comes to md5 hashing
            int comment = line.indexOf("#");
            String commentText = null;
            if (comment != -1) {
                commentText = line.substring(comment + 1);
                line = line.substring(0, comment);
            }
            int equals = line.indexOf("=");
            String declarationValue = null;
            if (equals != -1) {
                declarationValue = line.substring(equals + 1);
                line = line.substring(0, equals);
            }

            Matcher matcher = pattern.matcher(line);
            if (matcher.lookingAt()) {
                String typeName = matcher.group(1);
                boolean brackets = matcher.group(2) != null;
                int size = -1;
                if (matcher.group(3) != null)
                    size = Integer.valueOf(matcher.group(3));
                String key = matcher.group(4);
                
                RosType type = RosTypes.getType(name, typeName);
                if (brackets) {
                    if (size == -1)
                        type = RosTypes.getListType(type);
                    else
                        type = RosTypes.getListType(type, size);
                }
                fields.put(key, type);
                if (declarationValue != null) {
                    if ("string".equals(typeName))
                        constants.put(key, (declarationValue + "#" + commentText).trim());
                    else if (type instanceof RosPrimitiveType)
                        constants.put(key, declarationValue.trim());
                    else
                        throw new FormatException("Cannot set a constant value to a non-primitive type");
                }
            }
        }
    }

    public LinkedHashMap<String, RosType> getFields() {
        return fields;
    }
    
	public LinkedHashMap<String, String> getConstants() {
		return constants;
	}

    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    public boolean isPrimitive() {
        return false;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (this.getClass() != o.getClass())
            return false;
        RosMessageType other = (RosMessageType)o;
        return this.name.equals(other.name);
    }
    
    @Override
    public String toString() {
        return getName();
    }

    private String definition;
    
	public void readDefinition(String path) {
		try {
			this.definition = readFile(path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getDefinition() {
		return this.definition;
	}

	static String readFile(String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded);
	}
}
