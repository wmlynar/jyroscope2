package org.ros.rosjava.roslaunch.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.ros.rosjava.roslaunch.parsing.RosParamTag;
import org.ros.rosjava.roslaunch.xmlrpc.ObjectToXml;

import com.github.core.JyroscopeCore;
import com.github.jy2.ParameterClient;

public class Jyroscope2Util {

	private static JyroscopeCore jy2;
	private static ParameterClient parameterClient;

	public static void createInstance(String[] args) {

		HashMap<String, String> specialParameters = new HashMap<>();

		// parse ip,hostname
		String host = "127.0.0.1";

		String rosIp = System.getenv("ROS_IP");
		if (rosIp != null && !rosIp.isEmpty()) {
			host = rosIp;
		}
		String rosHostname = System.getenv("ROS_HOSTNAME");
		if (rosHostname != null && !rosHostname.isEmpty()) {
			host = rosHostname;
		}
		String specialParameterValue = specialParameters.get("ip");
		if (specialParameterValue != null && !specialParameterValue.isEmpty()) {
			host = specialParameterValue;
		}
		specialParameterValue = specialParameters.get("hostname");
		if (specialParameterValue != null && !specialParameterValue.isEmpty()) {
			host = specialParameterValue;
		}

		// parse master
		String master = "http://127.0.0.1:11311";

		String rosMasterUri = System.getenv("ROS_MASTER_URI");
		if (rosMasterUri != null && !rosMasterUri.isEmpty()) {
			master = rosMasterUri;
		}
		specialParameterValue = specialParameters.get("master");
		if (specialParameterValue != null) {
			master = specialParameterValue;
		}

		// disable it, now lazy loaded
		// RosTypeConverters.scanAnnotationsAndInitialize();

		jy2 = new JyroscopeCore();
		jy2.addRemoteMaster(master, host, "jylaunch");
		parameterClient = jy2.getParameterClient();
	}

	public static void setParameter(String name, Object value) {
		try {
			parameterClient.setParameter(name, value);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void deleteParameter(String name) {
		try {
			parameterClient.deleteParameter(name);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set the value of a YAML parameter on the server.
	 *
	 * @param rosparam is the RosParamTag to set
	 * @return the RosParamResponse data
	 * @throws an Exception if the request fails
	 */
	public static boolean setYamlParam(final RosParamTag rosparam) throws Exception {
		String paramName = rosparam.getParam();
		String resolved = rosparam.getResolvedName();
		Object yamlObj = rosparam.getYamlObject();

		// Dictionaries get a parameter set for each entry
		if (ObjectToXml.isMap(yamlObj)) {
			return _setMapParams(resolved, (Map<String, Object>) yamlObj);
		} else {
			// Otherwise handle all non-map rosparams the same way
			// return setParamObject(paramName, yamlObj);
			setParameter(paramName, yamlObj);
			return true;
		}
	}

	/**
	 * Set the value of all parameters contained within a dictionary.
	 *
	 * For example, given the following dictionary: {a: 1, b: hello, c: {sub:
	 * {another: value}
	 *
	 * The following parameters will be set:
	 *
	 * /a = 1 /b = hello /c/sub/another = value
	 *
	 * @param namespace is the parent namespace applied to all parameters
	 * @param map       is the Map object
	 * @return true if all parameters were set successfully, false if any failed
	 */
	@SuppressWarnings("unchecked")
	private static boolean _setMapParams(final String namespace, final Map<String, Object> map) {
		boolean success = true;
		for (Object key : map.keySet()) {
			Object value = map.get(key);
			String resolvedKey = RosUtil.joinNamespace(namespace, key.toString());

			if (ObjectToXml.isMap(value)) {
				// Recurse to handle this dictionary
				success |= _setMapParams(resolvedKey, (Map<String, Object>) value);
			} else {
//				// Found a final parameter, set it
//				String xml = ObjectToXml.objectToXml(value);
//
//				try {
//					setXmlParam(resolvedKey, xml);
//				}
//				catch (Exception e)
//				{
//					PrintLog.error("Failed to set param: " + resolvedKey + ": " + e.getMessage());
//					success = false;
//				}
				setParameter(resolvedKey, value);
			}
		}

		return success;
	}

}
