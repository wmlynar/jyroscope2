package org.ros.rosjava.roslaunch.launching;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.ros.rosjava.roslaunch.ArgumentParser;
import org.ros.rosjava.roslaunch.parsing.LaunchFile;
import org.ros.rosjava.roslaunch.util.RosUtil;
import org.ros.rosjava.roslaunch.logging.PrintLog;
import org.ros.rosjava.roslaunch.util.Jyroscope2Util;
import org.ros.rosjava.roslaunch.util.JarUtilsCollected;

/**
 * The RosLaunchRunner class
 *
 * This class is the main class that manages the launching
 * and monitoring of all running nodes.
 */
public class RosLaunchRunner
{
	/** The default port to reach the ROS master server. */
	public static final int DEFAULT_MASTER_PORT = 11311;
	/** The default number of workers for the rosmaster. */
	private static final int DEFAULT_NUM_WORKERS = 3;
	/** The default timeout to wait for the ROS master to start running before failing. */
	private static final long MASTER_START_TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(10);

	/** The run id for this process.*/
	private String m_runId;
	/** The parsed command line arguments. */
	private ArgumentParser m_parsedArgs;
	/** The port where the ROS master server is running / should run. */
	private int m_port;
	/** The number of workers to use when launching the rosmaster. */
	private int m_numWorkers;

	/** The ProcessMonitor object. */
	private ProcessMonitor m_processMonitor;

	/** The LaunchConfig object corresponding to this launch tree. */
	private LaunchConfig m_config;

	/**
	 * Constructor
	 *
	 * Create a RosLaunchRunner object.
	 *
	 * @param parsedArgs the parsed command line arguments
	 * @param launchFiles the List of LaunchFiles
	 */
	public RosLaunchRunner(
			final String runId,
			final ArgumentParser parsedArgs,
			final List<LaunchFile> launchFiles)
	{
		m_runId = runId;
		m_parsedArgs = parsedArgs;
		m_port = DEFAULT_MASTER_PORT;
		m_numWorkers = DEFAULT_NUM_WORKERS;

		// Load the config
		m_config = new LaunchConfig(
				m_runId, m_parsedArgs, launchFiles);

		// Create the process monitor
		m_processMonitor = new ProcessMonitor();

		// Handle the optional port command line argument
		int port = parsedArgs.getPort();
		if (port != -1)
		{
			// A custom port was specified, override the default uri
			String uri = RosUtil.createMasterUri(m_parsedArgs, m_port);
			m_config.setUri(uri);
		}

		// Handle the optional num workers command line argument
		int numWorkers = parsedArgs.getNumWorkers();
		if (numWorkers != -1) {
			m_numWorkers = numWorkers;
		}
	}

	/**
	 * Launch all of the nodes defined in the launch tree.
	 *
	 * @throws Exception if the launch fails
	 */
	public void launch() throws Exception
	{
		// Print the summary of parameters and nodes
		m_config.printSummary();

//		// Set up the core processes
//		setup();
//
//		// Now set all the parameters
//		m_config.setParameters();
//
//		// Then start all the non-core nodes
//		List<RosProcessIF> localProcesses = m_config.launchLocalNodes();

		// collects all jar urls
		List<RosProcessIF> localProcesses = m_config.launchLocalNodes();

		// create classloader using collected urls and set as context classloader
		JarUtilsCollected.createContextClassloader();

		// initialize hazelcast
		setup();

		// Now set all the parameters
		m_config.setParameters();

		// run all jar files
		JarUtilsCollected.runAllAddedJars();
	}

	/**
	 * Stop the running launch and all running nodes.
	 */
	public void stop()
	{
		// Stop all running processes
		m_processMonitor.shutdown();
	}

	/**
	 * Setup the launch.
	 */
	private void setup()
	{
		// Start up the core: master + core nodes defined in core.xml
		if (!m_config.isMasterRunning()) {
			this.launchMaster();  // Only if not already running
		}

		// Print the URI of the master on its own line
		String uri = m_config.getUri();
		PrintLog.bold("ROS_MASTER_URI=" + uri + "\n");

//		// start hazelcast instance
//		Jyroscope2Util.createInstance(args);
	}
	
	/**
	 * Launch the rosmaster process.
	 *
	 * @throws RuntimeException if the process did not start
	 * @throws RuntimeException if the rosmaster was never reached
	 */
	private void launchMaster()
	{
		String[] command = new String[]{
			"rosmaster",
			"--core",
			"-p", String.valueOf(m_port),
			"-w", String.valueOf(m_numWorkers)
		};

		PrintLog.info("auto-starting new master");

		Process master = null;
		try {
			master = Runtime.getRuntime().exec(command);
		}
		catch (IOException e)
		{
			throw new RuntimeException(
				"ERROR: unable to auto-start master process: " + e.getMessage());
		}

		// Generate the new URI for the master
		String uri = RosUtil.createMasterUri(m_parsedArgs, m_port);

		m_config.setUri(uri);  // Update the config with the new uri

		// Wait for the master to start for a certain amount of time before giving up
		long endTime = System.nanoTime() + MASTER_START_TIMEOUT_NANOS;
		while (!m_config.isMasterRunning() && System.nanoTime() < endTime) {
			try {
				Thread.sleep(100);
			}
			catch (Exception e) {
				// Ignore exceptions during sleep
			}
		}

		// Check if the master is still running
		if (!m_config.isMasterRunning()) {
			// Timed out...
			throw new RuntimeException("Could not contact master [" + uri + "]");
		}

		// Do not print the master streams
		RosLocalProcess rosMaster = new RosLocalProcess(
				"rosmaster",
				master,
				command,  // command used to run this process
				m_runId,
				true,  // this is a core process
				true,  // roslaunch does not require this node
				false);  // do not print output streams to the screen
		m_processMonitor.addProcess(rosMaster);
	}
}
