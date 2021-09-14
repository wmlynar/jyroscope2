package com.inovatica.orchestrator.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.ow2.proactive.process_tree_killer.ProcessTree;

import com.github.jy2.di.JyroscopeDi;
import com.inovatica.orchestrator.OutputCallback;
import com.inovatica.orchestrator.model.OrchestratorModelItem;

public class LaunchHandle {

	public static Log LOG = JyroscopeDi.getLog(LaunchHandle.class);

	private Process process;

	private Thread waitForProcessThread;
	private Thread inputThread;
	private Thread errorThread;

	private boolean shutdown = false;

	public HandleType type;

	private OrchestratorModelItem item;
	private boolean debug;

	private String javaOpts;

	private String jarParams;

	private String hostName;

	private boolean jmx;

	private String bashParams;

	private OutputCallback callback;

	private boolean heapDumpOnOutOfMemory;
	private String heapDumpPath;

	private boolean zGc;
	private boolean shenandoahGc;
	private boolean concurrentGc;

	private boolean killOnOutOfMemory;

	private int newRatio;

	private String user;
	private boolean runAsSudoWhenSuffix;

	private static String[] ENV_VARS;

	static {
		ENV_VARS = collectEnvironmentVariables();
	}

	boolean limitMemoryWhenXmx;
	Pattern memoryLimitPatern = Pattern.compile("xmx(\\d+)m");

	public LaunchHandle(OrchestratorModelItem item, String jarParams, String javaOpts, boolean debug, boolean jmx,
			String bashParams, String hostName, boolean heapDumpOnOutOfMemory, String heapDumpPath,
			boolean shenandoahGc, boolean concurrentGc, boolean killOnOutOfMemory, int newRatio, String user,
			boolean runAsSudoWhenSuffix, boolean limitMemoryWhenXmx, OutputCallback callback) {
		this.jarParams = jarParams;
		this.javaOpts = javaOpts;
		this.debug = debug;
		this.jmx = jmx;
		this.item = item;
		this.bashParams = bashParams;
		this.hostName = hostName;
		this.heapDumpOnOutOfMemory = heapDumpOnOutOfMemory;
		this.heapDumpPath = heapDumpPath;
		this.shenandoahGc = shenandoahGc;
		this.concurrentGc = concurrentGc;
		this.callback = callback;
		this.killOnOutOfMemory = killOnOutOfMemory;
		this.newRatio = newRatio;
		this.user = user;
		this.runAsSudoWhenSuffix = runAsSudoWhenSuffix;
		this.limitMemoryWhenXmx = limitMemoryWhenXmx;
	}

	public synchronized boolean start(HandleType type, String name, String fileName, File workingDir,
			boolean suspendDebug, boolean remoteProfiling, boolean useLegacyDebug, boolean zGc, int javaMemoryLimit) {

		this.zGc = zGc;

		String user = this.user;
		if (runAsSudoWhenSuffix && name.endsWith("sudo")) {
			user = "root";
		}

		if (limitMemoryWhenXmx) {
			Matcher memoryLimitMatcher = memoryLimitPatern.matcher(name);
			if (memoryLimitMatcher.find()) {
				javaMemoryLimit = Integer.parseInt(memoryLimitMatcher.group(1));
			}
		}

		String env = "";
//		if (debug || !javaOpts.trim().isEmpty() || jmx) {
		env = "JAVA_TOOL_OPTIONS=";
//		}
		if (!javaOpts.trim().isEmpty()) {
			env = env + javaOpts;
		}
		if (debug) {
			if (useLegacyDebug) {
				env = env + " -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=" + item.debugPort;
			} else {
				// made remote debugging address compatible with java 9+
				env = env + " -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=*:" + item.debugPort;
			}
			if (suspendDebug) {
				env = env + ",suspend=y ";
			} else {
				env = env + ",suspend=n ";
			}
		}
		if (remoteProfiling) {
			env = env
					+ " -XX:+UseLinuxPosixThreadCPUClocks -agentpath:/home/inovatica/remote_profiling/lib/deployed/jdk16/linux-amd64/libprofilerinterface.so=/home/inovatica/remote_profiling/lib,5140";
		}
		if (heapDumpOnOutOfMemory) {
			env = env + " -XX:+HeapDumpOnOutOfMemoryError";
		}
		if (heapDumpPath != null && !heapDumpPath.trim().isEmpty()) {
			env = env + " -XX:HeapDumpPath=" + heapDumpPath;
		}
		if (zGc) {
			env = env + " -XX:+UnlockExperimentalVMOptions -XX:+UseZGC -XX:+AlwaysPreTouch";
		} else if (shenandoahGc) {
			env = env
					+ " -XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC -XX:+AlwaysPreTouch -XX:+UseTransparentHugePages -XX:+UseNUMA";
		} else if (concurrentGc) {
			env = env + " -XX:+UseConcMarkSweepGC";
		}
		if (killOnOutOfMemory) {
			env = env + " -XX:+CrashOnOutOfMemoryError -XX:OnOutOfMemoryError=\"kill -9 %p\"";
		}
		if (newRatio > 0) {
			env = env + " -XX:NewRatio=" + newRatio;
		}
		if (javaMemoryLimit > 0) {
			env = env + " -Xmx" + javaMemoryLimit + "m";
		}

		// reduce thread stack size
		// env = env + " -Xss256k";

		if (jmx) {
			env = env + " -Dcom.sun.management.jmxremote.ssl=false"
					+ " -Dcom.sun.management.jmxremote.authenticate=false" + " -Dcom.sun.management.jmxremote.port="
					+ item.jmxPort + " -Dcom.sun.management.jmxremote.rmi.port=" + (item.jmxPort + 1)
					+ " -Djava.rmi.server.hostname=" + hostName + " -Dcom.sun.management.jmxremote.local.only=false";
		}

		String killMeEnvVar = "_KILL_ME=" + item.killMe + "_";
		String[] envWithKill = addToArray(ENV_VARS, killMeEnvVar);
		String[] envWithJavaOpts = addToArray(envWithKill, env);

		switch (type) {
		case LAUNCHFILE:
			LOG.info("Executing roslaunch " + fileName + " as " + name);
			try {
				String[] command = null;
				if (user == null || user.isEmpty()) {
					command = new String[] { "roslaunch", "--wait", "--screen", fileName };
				} else {
					// command = new String[] { "su", user, "-c", "roslaunch", fileName };
					command = new String[] { "gosu", user, "roslaunch", "--wait", "--screen", fileName };
				}
				process = Runtime.getRuntime().exec(command, envWithJavaOpts, workingDir);
			} catch (IOException e) {
				LOG.error("Exception caught while starting launchfile " + fileName, e);
				return false;
			}
			break;

		case ROS2_LAUNCHFILE:
			LOG.info("Executing ros2launch " + fileName + " as " + name);
			try {
				String[] command = null;
				if (user == null || user.isEmpty()) {
					command = new String[] { "ros2launch", fileName, "--env-var=" + env };
				} else {
					// command = new String[] { "su", user, "-c", "ros2launch", fileName,
					// "--env-var=" + env };
					command = new String[] { "gosu", user, "ros2launch", fileName, "--env-var=" + env };
				}
				// disabled debug exports, because ros2launch is in java
				// and subprocesses are in java
				// process = Runtime.getRuntime().exec(command, envWithJavaOpts, workingDir);
				process = Runtime.getRuntime().exec(command, envWithKill, workingDir);
			} catch (IOException e) {
				LOG.error("Exception caught while starting ros2 launchfile " + fileName, e);
				return false;
			}
			break;

		case HZ_LAUNCHFILE:
			LOG.info("Executing hzlaunch " + fileName + " as " + name);
			try {
				String[] command = null;
				if (user == null || user.isEmpty()) {
					command = new String[] { "hzlaunch", fileName };
				} else {
					// command = new String[] { "su", user, "-c", "roslaunch", fileName };
					command = new String[] { "gosu", user, "hzlaunch", fileName };
				}
				process = Runtime.getRuntime().exec(command, envWithJavaOpts, workingDir);
			} catch (IOException e) {
				LOG.error("Exception caught while starting launchfile " + fileName, e);
				return false;
			}
			break;

		case JY2_LAUNCHFILE:
			LOG.info("Executing jylaunch " + fileName + " as " + name);
			try {
				String[] command = null;
				if (user == null || user.isEmpty()) {
					command = new String[] { "jylaunch", "--wait", "--screen", fileName };
				} else {
					// command = new String[] { "su", user, "-c", "roslaunch", fileName };
					command = new String[] { "gosu", user, "jylaunch", "--wait", "--screen", fileName };
				}
				process = Runtime.getRuntime().exec(command, envWithJavaOpts, workingDir);
			} catch (IOException e) {
				LOG.error("Exception caught while starting launchfile " + fileName, e);
				return false;
			}
			break;

		case JARFILE:
			LOG.info("Executing jar " + fileName + " as " + name);
			try {
				String[] command;
				if (user == null || user.isEmpty()) {
					if (OsValidator.isUnix() || OsValidator.isMac() || OsValidator.isSolaris()) {
						command = new String[] { "bash", "-c", "java -jar " + fileName + " " + jarParams };
					} else {
						command = new String[] { "cmd", "/c", "java -jar " + fileName + " " + jarParams };
					}
				} else {
					// command = new String[] { "su", user, "-c", "bash", "-c", "java -jar " +
					// fileName + " " + jarParams };
					command = new String[] { "gosu", user, "bash", "-c", "java -jar " + fileName + " " + jarParams };
				}
				process = Runtime.getRuntime().exec(command, envWithJavaOpts, workingDir);
			} catch (IOException e) {
				LOG.error("Exception caught while starting jar " + fileName, e);
				return false;
			}
			break;

		case BASHFILE:
			LOG.info("Executing bash " + fileName + " as " + name);
			try {
				if (OsValidator.isUnix() || OsValidator.isMac() || OsValidator.isSolaris()) {
					process = Runtime.getRuntime().exec(new String[] { "chmod", "+x", fileName });
					try {
						process.waitFor();
					} catch (InterruptedException e) {
						LOG.error("Error while waiting for chmod, item " + fileName);
					}
					String[] command;
					if (user == null || user.isEmpty()) {
						command = new String[] { "bash", "-c", fileName + " " + bashParams };
					} else {
						// command = new String[] { "su", user, "-c", "bash", "-c", fileName + " " +
						// bashParams };
						command = new String[] { "gosu", user, "bash", "-c", fileName + " " + bashParams };
					}
					process = Runtime.getRuntime().exec(command, envWithKill, workingDir);
				} else {
					LOG.error("Cannot execute bash on non-unix system: " + OsValidator.OS + ", item " + fileName);
					return false;
				}
			} catch (IOException e) {
				LOG.error("Exception caught while starting bash " + fileName, e);
				return false;
			}
			break;

		default:
			LOG.error("Unknown " + fileName + " as " + name);
			return false;

		}
		synchronized (OrchestratorStartStop.monitor) {
			item.isStarted = true;
		}

		waitForProcessThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					process.waitFor();
				} catch (InterruptedException e) {
					if (!shutdown) {
						LOG.error("Exception caught while waiting for process: " + fileName
								+ ", shutting down process and output stream readers", e);
					}
				}
				// shutdown inputstream readers
				shutdown = true;
				waitForProcessThread.interrupt();
				inputThread.interrupt();
				errorThread.interrupt();
				synchronized (OrchestratorStartStop.monitor) {
					item.isStarted = false;
				}
			}
		});

		inputThread = new Thread(new Runnable() {

			@Override
			public void run() {
				InputStream is = process.getInputStream();
				BufferedReader bis = new BufferedReader(new InputStreamReader(is));
				try {
					String str = "";
					while (!shutdown && str != null) {
						str = bis.readLine();
						if (callback != null && str != null) {
							callback.logOutput(false, item.name, str);
						}
					}
				} catch (IOException e) {
					if (!shutdown) {
						LOG.error(
								"Exception caught while reading from input stream, shutting down process and output stream readers: "
										+ fileName,
								e);
					}
				} finally {
					try {
						bis.close();
					} catch (IOException e) {
						LOG.error("Exception caught when closing input stream", e);
					}
				}
				shutdown = true;
				waitForProcessThread.interrupt();
				inputThread.interrupt();
				errorThread.interrupt();
				synchronized (OrchestratorStartStop.monitor) {
					item.isStarted = false;
				}
			}
		});

		errorThread = new Thread(new Runnable() {

			@Override
			public void run() {
				InputStream es = process.getErrorStream();
				BufferedReader bis = new BufferedReader(new InputStreamReader(es));
				try {
					String str = "";
					while (!shutdown && str != null) {
						str = bis.readLine();
						if (callback != null && str != null) {
							callback.logOutput(true, item.name, str);
						}
					}
				} catch (IOException e) {
					if (!shutdown) {
						LOG.error(
								"Exception caught while reading from error stream, shutting down process and output stream readers: "
										+ fileName,
								e);
						shutdown = true;
					}
				} finally {
					try {
						bis.close();
					} catch (IOException e) {
						LOG.error("Exception caught when closing error stream", e);
					}
				}
				shutdown = true;
				waitForProcessThread.interrupt();
				inputThread.interrupt();
				errorThread.interrupt();
				synchronized (OrchestratorStartStop.monitor) {
					item.isStarted = false;
				}
			}
		});

		waitForProcessThread.start();
		inputThread.start();
		errorThread.start();

		return true;
	}

	public synchronized void stop(boolean destroyForcibly) {
		shutdown = true;

		if (destroyForcibly) {
			process.destroyForcibly();
		} else {
			process.destroy();
		}
		waitForProcessThread.interrupt();
		inputThread.interrupt();
		errorThread.interrupt();

		try {
			ProcessTree.get().killAll(Collections.singletonMap("_KILL_ME", item.killMe + "_"));
		} catch (InterruptedException e) {
			LOG.error("Exception while killing process", e);
		}
	}

	private static String[] collectEnvironmentVariables() {
		Map<String, String> vars = System.getenv();
		int size = vars.size();
		String[] newVars = new String[size];
		int i = 0;
		for (Entry<String, String> e : vars.entrySet()) {
			String str = e.getKey() + "=" + e.getValue();
			newVars[i++] = str;
		}
		return newVars;
	}

	private String[] addToArray(String[] array, String env) {
		String[] newArray = Arrays.copyOf(array, array.length + 1);
		newArray[array.length] = env;
		return newArray;
	}
}
