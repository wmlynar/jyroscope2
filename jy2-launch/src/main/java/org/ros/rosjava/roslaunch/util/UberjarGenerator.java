package org.ros.rosjava.roslaunch.util;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import com.github.jy2.ParameterClient;

public class UberjarGenerator {

	public static String generateMainClass(Map<String, String> paramsMap, ArrayList<String[]> classes,
			String outputClass) {

		String packageName = getPackageName(outputClass);
		String className = getClassName(outputClass);

		String output = generateTop(packageName, className);
		output += generateParameters(paramsMap);
		output += "\n";
		output += generateMainCalls(classes);
		output += generateBottom();

		return output;
	}

	private static String generateTop(String packageName, String className) {
		return "package " + packageName + ";\n" + "\n" + "import com.github.jy2.uberjar.Util;\n"
				+ "import com.github.jy2.ParameterClient;\n" + "import com.github.jy2.di.JyroscopeDi;\n"
				+ "import com.github.jy2.di.exceptions.CreationException;\n"
				+ "import com.github.jy2.log.NodeNameManager;\n" + "\n" + "public class " + className + " {\n" + "	\n"
				+ "	public static void main(String[] args) throws CreationException {\n" + "		\n"
				+ "		JyroscopeDi jyDi = new JyroscopeDi(\"autofork_uberjar\", args);\n"
				+ "		ParameterClient pc = jyDi.getParameterClient();\n\n";
	}

	private static String generateParameters(Map<String, String> paramsMap) {
		StringBuffer sb = new StringBuffer();
		for (Entry<String, String> entry : paramsMap.entrySet()) {
			sb.append("		pc.setParameter(\"");
			sb.append(entry.getKey());
			sb.append("\", \"");
			String value = entry.getValue().replace("\r", "\\r").replace("\n", "\\n");
			sb.append(value);
			sb.append("\");\n");
		}
		return sb.toString();
	}

	private static String generateMainCalls(ArrayList<String[]> classes) {
		StringBuffer sb = new StringBuffer();
		for (String[] cl : classes) {
			sb.append("		new Thread(new ThreadGroup(NodeNameManager.getNextThreadGroupName()), new Runnable() {\n"
					+ "			@Override\n" + "			public void run() {\n" + "				try {\n"
					+ "					");
			sb.append(cl[0]);
			sb.append(".main(new String[] {");
			for (int i = 1; i < cl.length; i++) {
				sb.append("\"");
				sb.append(cl[i]);
				sb.append("\"");
				if (i != cl.length - 1) {
					sb.append(",");
				}
			}
			sb.append("});\n" + "				} catch (Exception e) {\n" + "					e.printStackTrace();\n"
					+ "				}\n" + "			};\n" + "		}).start();\n");
		}
		return sb.toString();
	}

	private static String generateBottom() {
		return "\n" + "	}\n" + "}\n";
	}

	private static String getPackageName(String fullClassName) {
		int pos = fullClassName.lastIndexOf(".");
		if (pos < 0) {
			return "";
		}
		return fullClassName.substring(0, pos);
	}

	private static String getClassName(String fullClassName) {
		int pos = fullClassName.lastIndexOf(".");
		if (pos < 0) {
			return fullClassName;
		}
		return fullClassName.substring(pos + 1);
	}

}
