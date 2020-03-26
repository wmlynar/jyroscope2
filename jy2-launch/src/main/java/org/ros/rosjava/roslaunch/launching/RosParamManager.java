package org.ros.rosjava.roslaunch.launching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ros.rosjava.roslaunch.logging.PrintLog;
import org.ros.rosjava.roslaunch.parsing.GroupTag;
import org.ros.rosjava.roslaunch.parsing.IncludeTag;
import org.ros.rosjava.roslaunch.parsing.LaunchFile;
import org.ros.rosjava.roslaunch.parsing.NodeTag;
import org.ros.rosjava.roslaunch.parsing.RosParamTag;
import org.ros.rosjava.roslaunch.util.Jyroscope2Util;
import org.ros.rosjava.roslaunch.util.RosUtil;
import org.ros.rosjava.roslaunch.xmlrpc.ObjectToXml;
import org.ros.rosjava.roslaunch.xmlrpc.RosXmlRpcClient;

/**
 * The RosParamManager class
 *
 * This class is responsible for dealing with RosParamTags
 * defined within a launch file tree.
 */
public class RosParamManager
{
	//////////////////////////////////////////////
	// get params functions
	//

	/**
	 * Get the List of RosParamTags defined within the tree of
	 * launch files defined by the given List of LaunchFiles.
	 *
	 * @param launchFiles the List of LaunchFiles
	 * @return the List of RosParamTags
	 */
	public static List<RosParamTag> getRosParams(final List<LaunchFile> launchFiles)
	{
		List<RosParamTag> rosParams = new ArrayList<RosParamTag>();

		for (LaunchFile launchFile : launchFiles)
		{
			if (launchFile.isEnabled())
			{
				List<RosParamTag> launchParams = getRosParams(launchFile);
				rosParams.addAll(launchParams);
			}
		}

		return rosParams;
	}

	/**
	 * Get the List of RosParamTags defined within the given LaunchFile.
	 *
	 * @param launchFiles the LaunchFile
	 * @return the List of RosParamTags
	 */
	public static List<RosParamTag> getRosParams(final LaunchFile launchFile)
	{
		List<RosParamTag> rosParams = new ArrayList<RosParamTag>();

		// Stop if this launch file is disabled
		if (!launchFile.isEnabled()) return rosParams;

		// Add all rosparams defined in the launch tree
		for (RosParamTag rosParam : launchFile.getRosParams())
		{
			// Only get enabled rosparams
			if (rosParam.isEnabled()) {
				rosParams.add(rosParam);
			}
		}

		// Add all rosparams defined by nodes
		for (NodeTag node : launchFile.getNodes())
		{
			if (node.isEnabled())
			{
				for (RosParamTag rosParam : node.getRosParams())
				{
					// Only get enabled ros params
					if (rosParam.isEnabled()) {
						rosParams.add(rosParam);
					}
				}
			}
		}

		// Add all rosparams defined by groups
		for (GroupTag group : launchFile.getGroups())
		{
			if (group.isEnabled())
			{
				List<RosParamTag> groupParams = getRosParams(group.getLaunchFile());
				rosParams.addAll(groupParams);
			}
		}

		// Add all rosparams defined in includes
		for (IncludeTag include : launchFile.getIncludes())
		{
			if (include.isEnabled())
			{
				List<RosParamTag> includeParams = getRosParams(include.getLaunchFile());
				rosParams.addAll(includeParams);
			}
		}

		return rosParams;
	}

	/**
	 * Get the Map of rosparam names to rosparam values for all RosParamTags
	 * that load a parameter for the given launch file tree.
	 *
	 * @param launchFiles the List of LaunchFiles
	 * @return the Map of 'load' rosparam name value pairs
	 */
	public static Map<String, String> getLoadRosParamsMap(final List<LaunchFile> launchFiles)
	{
		Map<String, String> loadParams = new HashMap<String, String>();

		for (LaunchFile launchFile : launchFiles)
		{
			if (launchFile.isEnabled()) {
				getLoadRosParamsMap(launchFile, loadParams);
			}
		}

		return loadParams;
	}

	/**
	 * Get the Map of rosparam names to rosparam values for all RosParamTags
	 * that load a parameter for the given LaunchFile.
	 *
	 * @param launchFiles the LaunchFile
	 * @param loadParams the Map of 'load' rosparam name value pairs
	 */
	public static void getLoadRosParamsMap(
			final LaunchFile launchFile,
			Map<String, String> loadParams)
	{
		// Stop if the launch file is disabled
		if (!launchFile.isEnabled()) return;

		// Add all rosparams defined in the launch tree
		for (RosParamTag rosParam : launchFile.getRosParams())
		{
			if (rosParam.isEnabled() && rosParam.isLoadCommand()) {
				getLoadRosParam(rosParam, loadParams);
			}
		}

		// Add all rosparams defined by nodes
		for (NodeTag node : launchFile.getNodes())
		{
			if (node.isEnabled())
			{
				for (RosParamTag rosParam : node.getRosParams())
				{
					if (rosParam.isEnabled() && rosParam.isLoadCommand()) {
						getLoadRosParam(rosParam, loadParams);
					}
				}
			}
		}

		// Add all rosparams defined by groups
		for (GroupTag group : launchFile.getGroups())
		{
			if (group.isEnabled()) {
				getLoadRosParamsMap(group.getLaunchFile(), loadParams);
			}
		}

		// Add all rosparams defined in includes
		for (IncludeTag include : launchFile.getIncludes())
		{
			if (include.isEnabled()) {
				getLoadRosParamsMap(include.getLaunchFile(), loadParams);
			}
		}
	}
	/**
	 * Get a map of rosparam name value pairs for a single 'load' RosParamTag.
	 *
	 * @param rosParam the RosParamTag
	 * @param loadParams the Map of 'load' rosparam name value pairs
	 */
	@SuppressWarnings("unchecked")
	public static void getLoadRosParam(
			final RosParamTag rosParam,
			Map<String, String> loadParams)
	{
		if (rosParam.isEnabled())
		{
			///// must be a load command
			String resolved = rosParam.getResolvedName();
			Object yamlObj = rosParam.getYamlObject();

			String content = rosParam.getYamlContent();

			if (resolved.length() > 0 && content.length() > 0)
			{
				// Handle dumping dictionary parameters, which end up
				// dumping parameters based on the layout of the dictionary
				if (yamlObj != null && ObjectToXml.isMap(yamlObj))
				{
					getLoadRosParamDict(
							resolved,
							(Map<String, Object>)yamlObj,
							loadParams);
					return;
				}
				else {
					// Store the non-dictionary parameter
					loadParams.put(resolved, content);
				}
			}
		}
	}

	/**
	 * Get a Map of rosparam name value pairs for all the parameters
	 * that will be loaded by a RosParamTag with a dictionary (i.e., Map)
	 * value.
	 *
	 * @param namespace the namespace of the RosParamTag
	 * @param map the Map value of the RosParamTag
	 * @param loadParams the Map of rosparam name value pairs
	 */
	@SuppressWarnings("unchecked")
	private static void getLoadRosParamDict(
			final String namespace,
			final Map<String, Object> map,
			Map<String, String> loadParams)
	{
		for (Object key : map.keySet())
		{
			Object value = map.get(key);
			String resolvedKey = RosUtil.joinNamespace(namespace, key.toString());

			if (ObjectToXml.isMap(value))
			{
				// Recurse to handle this dictionary
				getLoadRosParamDict(
					resolvedKey, (Map<String, Object>)value, loadParams);
			}
			else
			{
				// Found a parameter, print it
				loadParams.put(resolvedKey, value.toString());
			}
		}
	}


	//////////////////////////////////////////////
	// set functions
	//

	/**
	 * Send a request to the ROS master server to set all of
	 * the rosparams defined in the given List of RosParamTags.
	 *
	 * @param rosParams the List of RosParamTags
	 * @param uri the URI to reach the ROS master server
	 * @throws Exception if one of the rosparams failed to set
	 */
	public static void setParameters(
			final List<RosParamTag> rosParams,
			final String uri) throws Exception
	{
		for (RosParamTag rosParam: rosParams)
		{
			if (rosParam.isEnabled() && rosParam.isLoadCommand()) {
				setRosParam(rosParam, uri);
			}
		}
	}

	/**
	 * Send a request to the ROS master server to set the value of
	 * a single RosParamTag.
	 *
	 * @param rosParam the RosParamTag
	 * @param uri the URI to reach the ROS master server
	 * @throws Exception if the rosparam failed to be set
	 */
	private static void setRosParam(final RosParamTag rosParam, final String uri) throws Exception
	{
		if (rosParam.isEnabled())
		{
			///// must be a load command
			String resolved = rosParam.getResolvedName();
			Object yamlObj = rosParam.getYamlObject();

			if (resolved.length() > 0 && yamlObj != null)
			{
				RosXmlRpcClient client = new RosXmlRpcClient(uri);
				client.setYamlParam(rosParam);
//				
//				Jyroscope2Util.setYamlParam(rosParam);
			}
		}
	}


	//////////////////////////////////////////////
	// rosparam delete functions
	//

	/**
	 * Send a request to the ROS master server to delete all
	 * of the RosParamTags defined in the given List.
	 *
	 * @param rosParams the List of RosParamTags
	 * @param uri the URI to reach the ROS master server
	 */
	public static void deleteParameters(
			final List<RosParamTag> rosParams,
			final String uri)
	{
		for (RosParamTag rosParam : rosParams)
		{
			if (rosParam.isEnabled() && rosParam.isDeleteCommand()) {
				deleteRosParam(rosParam, uri);
			}
		}
	}

	/**
	 * Send a request to the ROS master server to delete a
	 * single RosParamTag.
	 *
	 * @param rosParam the RosParamTag to delete
	 * @param uri the URI to reach the ROS master server
	 */
	private static void deleteRosParam(final RosParamTag rosParam, final String uri)
	{
		if (rosParam.isEnabled())
		{
			String param = rosParam.getResolvedName();

			PrintLog.info("running rosparam delete " + param);

			RosXmlRpcClient client = new RosXmlRpcClient(uri);

			try
			{
				// Handle the generic delete differently than
				// normal delete params
				if (param.compareTo("/") == 0) {
					client.clearParam(param);
				}
				else {
					client.deleteParam(param);
				}
			}
			catch (Exception e) {
				PrintLog.error(e.getMessage());
			}
			
//			Jyroscope2Util.deleteParameter(param);
		}
	}


	//////////////////////////////////////////////
	// rosparam dump functions
	//

	/**
	 * Run all of the 'dump' RosParamTags defined in the given
	 * List of RosParamTags.
	 *
	 * @param rosParams the List of RosParamTags
	 * @param uri the URI to reach the ROS master server
	 */
	public static void dumpParameters(
			final List<RosParamTag> rosParams,
			final String uri)
	{
		for (RosParamTag rosParam : rosParams)
		{
			if (rosParam.isEnabled() && rosParam.isDumpCommand()) {
				dumpParam(rosParam);
			}
		}
	}

	/**
	 * Run a single 'dump' rosparam command.
	 *
	 * @param rosParam the RosParamTag
	 */
	public static void dumpParam(final RosParamTag rosParam)
	{
		if (rosParam.isEnabled())
		{
			String file = rosParam.getFile();

			// Make sure the file exists
			if (file != null && file.length() > 0)
			{
				List<String> fullCommand = new ArrayList<String>();

				String resolvedName = rosParam.getResolvedName();

				// Create the command to dump the rosparam to the desired file
				fullCommand.add("rosparam");
				fullCommand.add("dump");
				fullCommand.add(file);
				fullCommand.add(resolvedName);

				// Convert the list of command args to an array
				String[] command = new String[fullCommand.size()];
				fullCommand.toArray(command);

				PrintLog.info("running rosparam dump " + file + " " + resolvedName);

				Process proc;
				try {
					proc = Runtime.getRuntime().exec(command);

					// Wait for the process to complete -- should be fast
					proc.waitFor();
				}
				catch (Exception e)
				{
					String msg = "ERROR: while running: rosparam dump " + file + " " + resolvedName;
					msg += "\n" + e.getMessage();

					PrintLog.error(msg);
				}
			}
		}
	}
}
