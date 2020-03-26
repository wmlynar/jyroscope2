package com.jyroscope.ros.types;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.jyroscope.FormatException;
import com.jyroscope.Log;

public class RosTypes {
    
	private static final ArrayList<File> msgPaths;
    private static final HashMap<String, RosType> types;
    
    static {
        msgPaths = new ArrayList<>();

        // Pre-populate the types with primitives
        types = new HashMap<>();
        for (RosType type : RosPrimitiveType.values())
            types.put(type.getName(), type);
        types.put(RosStringType.INSTANCE.getName(), RosStringType.INSTANCE);
    }

	public static void addMsgSearchPath(File path) {
		msgPaths.add(path);
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
				File file = getMsgFile(typeName);
				if (file == null) {
					if (context != null && context.contains("/")) {
						String prefixedName = context.substring(0, context.lastIndexOf("/") + 1) + typeName;
						file = getMsgFile(prefixedName);
						if (file != null)
							typeName = prefixedName;
					}
					// still can't find a file?
					if (file == null)
						throw new FileNotFoundException(
								"Msg definition for " + typeName + " not found in " + msgPaths.toString());
				}

				RosMessageType compound = new RosMessageType(typeName);
				compound.parseMsgFile(new FileReader(file));
				compound.readDefinition(file.getAbsolutePath());
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

}

