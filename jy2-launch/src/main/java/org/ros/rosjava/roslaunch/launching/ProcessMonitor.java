package org.ros.rosjava.roslaunch.launching;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.ros.rosjava.roslaunch.logging.PrintLog;

/**
 * The ProcessMonitor class
 *
 * This class is responsible for monitoring a set of
 * running processes to determine if they have died
 * or not.
 */
public class ProcessMonitor
{
	/** The List of running RosProcesses that will be monitored. */
	List<RosProcessIF> m_processes;
	/** The List of running RosProcesses that are dead. */
	List<RosProcessIF> m_deadProcesses;
	/** The Map from running RosProcesses that are being respawned to time of death. */
	Map<RosProcessIF, Long> m_respawningProcesses;

	/** True if the process monitor has been shutdown, or detected that it needs to shutdown. */
	private boolean m_isShutdown;

	/** Semaphore for protecting process monitor data. */
	private Semaphore m_semaphore;

	/**
	 * Constructor
	 *
	 * Create a ProcessMonitor object.
	 */
	public ProcessMonitor()
	{
		m_isShutdown = false;
		m_semaphore = new Semaphore(1);
		m_processes = new ArrayList<RosProcessIF>();
	}

	/**
	 * Add a List of processes to be monitored.
	 *
	 * @param process the List of RosProcesses to monitor
	 */
	public void addProcesses(final List<RosProcessIF> processes)
	{
		m_processes.addAll(processes);
	}

	/**
	 * Add a single process to be monitored.
	 *
	 * @param process the RosProcess to monitor
	 */
	public void addProcess(final RosProcessIF process)
	{
		m_processes.add(process);
	}

	/**
	 * Determine if the ProcessMonitor has detected the need
	 * to shutdown, or been shutdown itself.
	 *
	 * @return true if application should shutdown, false otherwise
	 */
	public boolean isShutdown()
	{
		return m_isShutdown;
	}

	/**
	 * Stop all running processes.
	 */
	public void shutdown()
	{
		// Acquire the lock -- no matter how long it takes
		while (!m_semaphore.tryAcquire()) {
			try { Thread.sleep(100); } catch (Exception e) { /* Ignore sleep errors */ }
		}

		// Only shutdown once
		if (!m_isShutdown)
		{
			// Kill all running processes
			for (RosProcessIF proc : m_processes)
			{
				if (proc.isRunning()) {
					PrintLog.info("[" + proc.getName() + "] killing on exit");
					proc.destroy();
				}
			}

			// Wait for all processes to stop
			for (RosProcessIF proc : m_processes)
			{
				try {
					proc.waitFor();
				} catch (Exception e) {
					PrintLog.error("Error while waiting for process to stop: " + e.getMessage());
				}
			}

			m_isShutdown = true;

			PrintLog.info("shutting down processing monitor...");
			PrintLog.info("... shutting down processing monitor complete");
			PrintLog.bold("done");
		}

		m_semaphore.release();  // Release the lock
	}
}
