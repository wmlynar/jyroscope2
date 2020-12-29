package com.jyroscope.ros.types;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import com.jyroscope.FormatException;
import com.jyroscope.Log;

public class RosTypes {
    
	private static final ArrayList<File> msgPaths;
	private static final ArrayList<String> msgResourcePaths;
    private static final HashMap<String, RosType> types;
    
    static {
        msgPaths = new ArrayList<>();
        msgResourcePaths = new ArrayList<>();

        // Pre-populate the types with primitives
        types = new HashMap<>();
        for (RosType type : RosPrimitiveType.values())
            types.put(type.getName(), type);
        types.put(RosStringType.INSTANCE.getName(), RosStringType.INSTANCE);
    }

	public static void addMsgSearchPath(File path) {
		msgPaths.add(path);
    }

	public static void addMsgResourceSearchPath(String path) {
		msgResourcePaths.add(path);
	}

    public static RosMessageType getMessageType(String typeName) {
        return (RosMessageType)getType(typeName);
    }
    
    public static RosListType getListType(RosType base) {
        return new RosListType(base);
    }
    
    public static RosListType getListType(RosType base, int length) {
        return new RosListType(base, length);
    }
    
    public static RosListType getListType(String typeName) {
        return getListType(getType(typeName));
    }
    
    public static RosListType getListType(String typeName, int length) {
        return getListType(getType(typeName), length);
    }
    
    public static RosType getType(String typeName) {
        return getType(null, typeName);
    }
    
    public static RosType getType(String context, String typeName) {
        // TODO handle ROS type "*", used by (rostopic hz /topicname)
        if ("Header".equals(typeName))
            typeName = "std_msgs/Header";
        if (!types.containsKey(typeName)) {
            try {
            	String msg = null;
				File file = getMsgFile(typeName);
				if (file == null) {
					if (context != null && context.contains("/")) {
						String prefixedName = context.substring(0, context.lastIndexOf("/") + 1) + typeName;
						file = getMsgFile(prefixedName);
						if (file != null)
							typeName = prefixedName;
					}
				}
				if (file != null) {
					byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
					msg = new String(encoded);
				} else {
					InputStream stream = getMsgResource(typeName);
					if (stream == null) {
						if (context != null && context.contains("/")) {
							String prefixedName = context.substring(0, context.lastIndexOf("/") + 1) + typeName;
							stream = getMsgResource(prefixedName);
							if (stream != null)
								typeName = prefixedName;
						}
					}
					if (stream != null) {
						ByteArrayOutputStream result = new ByteArrayOutputStream();
						byte[] buffer = new byte[1024];
						int length;
						while ((length = stream.read(buffer)) != -1) {
							result.write(buffer, 0, length);
						}
						// StandardCharsets.UTF_8.name() > JDK 7
						msg = result.toString("UTF-8");
					}
				}
				
				// still can't find a file?
				if (msg == null)
					throw new FileNotFoundException(
							"Msg definition for " + typeName + " not found in " + msgPaths.toString()
									+ " or in resources in " + msgResourcePaths.toString());
				
				RosMessageType compound = new RosMessageType(typeName);
				compound.parseMsgFile(new StringReader(msg));
				compound.setDefinition(msg);
				types.put(typeName, compound);
            } catch (FormatException fe) {
                Log.exception(RosTypes.class, fe, "Error parsing message definition for type " + typeName);
            } catch (IOException ioe) {
                Log.exception(RosTypes.class, ioe, "Error loading message definition for type " + typeName);
            }
        }
        return types.get(typeName);
    }

	private static File getMsgFile(String typeName) {
		for (File path : msgPaths) {
			File file = new File(path, typeName + ".msg");
			if (file.exists())
				return file;
            int slash = typeName.lastIndexOf('/');
            if (slash != -1) {
				file = new File(new File(path, typeName.substring(0, slash)),
						"msg" + typeName.substring(slash) + ".msg");
				if (file.exists())
					return file;
            }
        }
        return null;
    }

	private static InputStream getMsgResource(String typeName) {
		for (String path : msgResourcePaths) {
			InputStream stream = RosTypes.class.getResourceAsStream("/" + path + typeName + ".msg");
			if (stream != null) {
				return stream;
			}
			int slash = typeName.lastIndexOf('/');
			if (slash != -1) {
				stream = RosTypes.class.getResourceAsStream(
						"/" + path + "/" + typeName.substring(0, slash) + "/msg" + typeName.substring(slash) + ".msg");
				if (stream != null) {
					return stream;
				}
			}
		}
		return null;
	}
}

