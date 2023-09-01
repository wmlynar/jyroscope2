package com.inovatica.orchestrator.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import com.github.jy2.di.JyroscopeDi;
import com.inovatica.orchestrator.internal.DirectoryScanner;
import com.inovatica.orchestrator.internal.HandleType;

public class OrchestratorModelBuilder {

	public int debugStartPort = 4001;
	public int jmxStartPort = 9012;

	public static Log LOG = JyroscopeDi.getLog();

	private String launchFileDirLocal1 = "./";
	private String ros2LaunchFileDirLocal1 = "./";
	private String hzLaunchFileDirLocal1 = "./";
	private String jy2LaunchFileDirLocal1 = "./";
	private String jarFileDirLocal1 = "./";
	private String bashFileDirLocal1 = "./";
	private String launchFileExtension1 = ".launch";
	private String ros2launchFileExtension1 = ".launch2";
	private String hzlaunchFileExtension1 = ".launchhz";
	private String jy2launchFileExtension1 = ".launchjy";
	private String jarFileExtension1 = ".jar";
	private String bashFileExtension1 = ".bash";
	private ArrayList<String> fileStartList = new ArrayList<>();
	private boolean debug = false;
	private String javaOpts = "";
	private String jarParams = "";

	public DirectoryScanner directoryScanner = new DirectoryScanner();

	private boolean jmx;
	private String bashParams;
	private String workingDir = ".";
	private String hostname = "localhost";
	private boolean heapDumpOnOutOfMemomry;
	private String heapDumpPath;
	public boolean shenandoahGc;
	public boolean concurrentGc;
	public boolean optimizeGc;
	public boolean preallocateGc;
	public boolean killOnOutOfMemory;
	public boolean allowChangingNice;

	public OrchestratorModelBuilder setLaunchFileExtension(String string) {
		launchFileExtension1 = string;
		return this;
	}

	public OrchestratorModelBuilder setRos2LaunchFileExtension(String string) {
		ros2launchFileExtension1 = string;
		return this;
	}

	public OrchestratorModelBuilder setJarFileExtension(String string) {
		jarFileExtension1 = string;
		return this;
	}

	public OrchestratorModelBuilder setLaunchFileDir(String string) {
		string = string.trim();
		if (!string.isEmpty() && !string.endsWith("/")) {
			string = string + "/";
		}
		launchFileDirLocal1 = string;
		return this;
	}

	public OrchestratorModelBuilder setRos2LaunchFileDir(String string) {
		string = string.trim();
		if (!string.isEmpty() && !string.endsWith("/")) {
			string = string + "/";
		}
		ros2LaunchFileDirLocal1 = string;
		return this;
	}

	public OrchestratorModelBuilder setHzLaunchFileDir(String string) {
		string = string.trim();
		if (!string.isEmpty() && !string.endsWith("/")) {
			string = string + "/";
		}
		hzLaunchFileDirLocal1 = string;
		return this;
	}

	public OrchestratorModelBuilder setJy2LaunchFileDir(String string) {
		string = string.trim();
		if (!string.isEmpty() && !string.endsWith("/")) {
			string = string + "/";
		}
		jy2LaunchFileDirLocal1 = string;
		return this;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setJmx(boolean jmx) {
		this.jmx = jmx;
	}

	public void setJavaOpts(String javaOpts) {
		this.javaOpts = javaOpts;
	}

	public void setJarParams(String jarParams) {
		this.jarParams = jarParams;
	}

	public void setBashParams(String bashParams) {
		this.bashParams = bashParams;
	}

	public OrchestratorModelBuilder setJarFileDir(String string) {
		string = string.trim();
		if (!string.isEmpty() && !string.endsWith("/")) {
			string = string + "/";
		}
		jarFileDirLocal1 = string;
		return this;
	}

	public void setHostname(String hostname) {
		this.hostname  = hostname;
	}
	
	public void setHeapDumpOnOutOfMemomry(boolean heapDumpOnOutOfMemomry) {
		this.heapDumpOnOutOfMemomry = heapDumpOnOutOfMemomry;
	}

	public void setHeapDumpPath(String heapDumpPath) {
		this.heapDumpPath = heapDumpPath;
	}
	
	public OrchestratorModelBuilder setBashFileDir(String string) {
		string = string.trim();
		if (!string.isEmpty() && !string.endsWith("/")) {
			string = string + "/";
		}
		bashFileDirLocal1 = string;
		return this;
	}

	public void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
	}

	public OrchestratorModel build() {
		OrchestratorModel model = new OrchestratorModel();
		model.launchFileExtension = launchFileExtension1;
		model.ros2launchFileExtension = ros2launchFileExtension1;
		model.jarFileExtension = jarFileExtension1;
		model.launchFileDirLocal = launchFileDirLocal1;
		model.ros2LaunchFileDirLocal = ros2LaunchFileDirLocal1;
		model.hzLaunchFileDirLocal = hzLaunchFileDirLocal1;
		model.jy2LaunchFileDirLocal = jy2LaunchFileDirLocal1;
		model.jarFileDirLocal = jarFileDirLocal1;
		model.bashFileDirLocal = bashFileDirLocal1;
		model.jarParams = jarParams;
		model.javaOpts = javaOpts;
		model.debug = debug;
		model.jmx = jmx;
		model.bashParams = bashParams;
		model.hostName = hostname;
		model.heapDumpOnOutOfMemomry = heapDumpOnOutOfMemomry;
		model.heapDumpPath = heapDumpPath;
		model.shenandoahGc = shenandoahGc;
		model.concurrentGc = concurrentGc;
		model.optimizeGc = optimizeGc;
		model.preallocateGc = preallocateGc;
		model.killOnOutOfMemory = killOnOutOfMemory;
		model.allowChangingNice = allowChangingNice;
		model.workingDir = new File(workingDir);

		String launchFileDir = new File(launchFileDirLocal1).getAbsolutePath();
		String ros2LaunchFileDir = new File(ros2LaunchFileDirLocal1).getAbsolutePath();
		String hzLaunchFileDir = new File(hzLaunchFileDirLocal1).getAbsolutePath();
		String jy2LaunchFileDir = new File(jy2LaunchFileDirLocal1).getAbsolutePath();
		String jarFileDir = new File(jarFileDirLocal1).getAbsolutePath();
		String bashFileDir = new File(bashFileDirLocal1).getAbsolutePath();

		ArrayList<String> launchFileNames = directoryScanner.scanDirectoryWithoutExtension(launchFileDir,
				launchFileExtension1);
		
		ArrayList<String> ros2launchFileNames = directoryScanner.scanDirectoryWithoutExtension(ros2LaunchFileDir,
				ros2launchFileExtension1);
		
		ArrayList<String> hzlaunchFileNames = directoryScanner.scanDirectoryWithoutExtension(hzLaunchFileDir,
				hzlaunchFileExtension1);
		
		ArrayList<String> jy2launchFileNames = directoryScanner.scanDirectoryWithoutExtension(jy2LaunchFileDir,
				jy2launchFileExtension1);
		
		ArrayList<String> jarFileNames = directoryScanner.scanDirectoryWithoutExtension(jarFileDir, jarFileExtension1);

		ArrayList<String> bashFileNames = directoryScanner.scanDirectoryWithoutExtension(bashFileDir,
				bashFileExtension1);

		int debugPort = debugStartPort;
		int jmxPort = jmxStartPort;
		
		int killMe = 1;

		for (String launchFileName : launchFileNames) {
			if(model.containsItemWithName(launchFileName)) {
				LOG.error("Found two launch items with the same name, ignoring: " + launchFileName);
				continue;
			}
			
			OrchestratorModelItem item = new OrchestratorModelItem();
			item.name = launchFileName;
			item.absolutePath = launchFileDir + "/" + launchFileName + launchFileExtension1;
			item.type = HandleType.LAUNCHFILE;
			item.isStarted = false;
			item.onStart = false;
			item.debugPort = debugPort++;
			item.jmxPort = jmxPort;
			item.killMe = killMe++;
			jmxPort += 2;
			model.add(item);
		}

		for (String launchFileName : ros2launchFileNames) {
			if(model.containsItemWithName(launchFileName)) {
				LOG.error("Found two launch items with the same name, ignoring: " + launchFileName);
				continue;
			}
			
			OrchestratorModelItem item = new OrchestratorModelItem();
			item.name = launchFileName;
			item.absolutePath = ros2LaunchFileDir + "/" + launchFileName + ros2launchFileExtension1;
			item.type = HandleType.ROS2_LAUNCHFILE;
			item.isStarted = false;
			item.onStart = false;
			item.debugPort = debugPort++;
			item.jmxPort = jmxPort;
			item.killMe = killMe++;
			jmxPort += 2;
			model.add(item);
		}

		for (String launchFileName : hzlaunchFileNames) {
			if(model.containsItemWithName(launchFileName)) {
				LOG.error("Found two launch items with the same name, ignoring: " + launchFileName);
				continue;
			}
			
			OrchestratorModelItem item = new OrchestratorModelItem();
			item.name = launchFileName;
			item.absolutePath = hzLaunchFileDir + "/" + launchFileName + hzlaunchFileExtension1;
			item.type = HandleType.HZ_LAUNCHFILE;
			item.isStarted = false;
			item.onStart = false;
			item.debugPort = debugPort++;
			item.jmxPort = jmxPort;
			item.killMe = killMe++;
			jmxPort += 2;
			model.add(item);
		}

		for (String launchFileName : jy2launchFileNames) {
			if(model.containsItemWithName(launchFileName)) {
				LOG.error("Found two launch items with the same name, ignoring: " + launchFileName);
				continue;
			}
			
			OrchestratorModelItem item = new OrchestratorModelItem();
			item.name = launchFileName;
			item.absolutePath = jy2LaunchFileDir + "/" + launchFileName + jy2launchFileExtension1;
			item.type = HandleType.JY2_LAUNCHFILE;
			item.isStarted = false;
			item.onStart = false;
			item.debugPort = debugPort++;
			item.jmxPort = jmxPort;
			item.killMe = killMe++;
			jmxPort += 2;
			model.add(item);
		}

		for (String jarFileName : jarFileNames) {
			if(model.containsItemWithName(jarFileName)) {
				LOG.error("Found two launch items with the same name, ignoring: " + jarFileName);
				continue;
			}
			
			OrchestratorModelItem item = new OrchestratorModelItem();
			item.name = jarFileName;
			item.absolutePath = jarFileDir + "/" + jarFileName + jarFileExtension1;
			item.type = HandleType.JARFILE;
			item.isStarted = false;
			item.onStart = false;
			item.debugPort = debugPort++;
			item.jmxPort = jmxPort;
			item.killMe = killMe++;
			jmxPort += 2;
			model.add(item);
		}

		for (String bashFileName : bashFileNames) {
			if(model.containsItemWithName(bashFileName)) {
				LOG.error("Found two launch items with the same name, ignoring: " + bashFileName);
				continue;
			}
			
			OrchestratorModelItem item = new OrchestratorModelItem();
			item.name = bashFileName;
			item.absolutePath = bashFileDir + "/" + bashFileName + bashFileExtension1;
			item.type = HandleType.BASHFILE;
			item.isStarted = false;
			item.onStart = false;
			item.debugPort = debugPort++;
			item.jmxPort = jmxPort;
			item.killMe = killMe++;
			jmxPort += 2;
			model.add(item);
		}

		for (String string : fileStartList) {
			OrchestratorModelItem m = model.getByName(string);
			if (m == null) {
				LOG.error("Launchfile start list, cannot find launchfile item: " + string);
				continue;
			}
			m.onStart = true;
		}
		
		model.nextDebugPort = debugPort;
		model.nextJmxPort = jmxPort;
		model.nextKillMe = killMe;

		return model;
	}

	public void setStringStartList(List<?> startList) {
		for (Object i : startList) {
			fileStartList.add(i.toString());
		}
	}

	/**
	 * Rescan folder and add new files to the model.
	 */
	public void scanAndAddNewFiles(OrchestratorModel model) {
		String launchFileDir = new File(launchFileDirLocal1).getAbsolutePath();
		String ros2LaunchFileDir = new File(ros2LaunchFileDirLocal1).getAbsolutePath();
		String hzLaunchFileDir = new File(hzLaunchFileDirLocal1).getAbsolutePath();
		String jy2LaunchFileDir = new File(jy2LaunchFileDirLocal1).getAbsolutePath();
		String jarFileDir = new File(jarFileDirLocal1).getAbsolutePath();
		String bashFileDir = new File(bashFileDirLocal1).getAbsolutePath();

		ArrayList<String> launchFileNames = directoryScanner.scanDirectoryWithoutExtension(launchFileDir,
				launchFileExtension1);
		
		ArrayList<String> ros2launchFileNames = directoryScanner.scanDirectoryWithoutExtension(ros2LaunchFileDir,
				ros2launchFileExtension1);
		
		ArrayList<String> hzlaunchFileNames = directoryScanner.scanDirectoryWithoutExtension(hzLaunchFileDir,
				hzlaunchFileExtension1);
		
		ArrayList<String> jy2launchFileNames = directoryScanner.scanDirectoryWithoutExtension(jy2LaunchFileDir,
				jy2launchFileExtension1);
		
		ArrayList<String> jarFileNames = directoryScanner.scanDirectoryWithoutExtension(jarFileDir, jarFileExtension1);

		ArrayList<String> bashFileNames = directoryScanner.scanDirectoryWithoutExtension(bashFileDir,
				bashFileExtension1);

		int debugPort = model.nextDebugPort;
		int jmxPort = model.nextJmxPort;
		int killMe = model.nextKillMe;

		for (String launchFileName : launchFileNames) {
			if(model.containsItemWithName(launchFileName)) {
				continue;
			}
			
			OrchestratorModelItem item = new OrchestratorModelItem();
			item.name = launchFileName;
			item.absolutePath = launchFileDir + "/" + launchFileName + launchFileExtension1;
			item.type = HandleType.LAUNCHFILE;
			item.isStarted = false;
			item.onStart = false;
			item.debugPort = debugPort++;
			item.jmxPort = jmxPort;
			item.killMe = killMe++;
			jmxPort += 2;
			model.add(item);
		}

		for (String launchFileName : ros2launchFileNames) {
			if(model.containsItemWithName(launchFileName)) {
				continue;
			}
			
			OrchestratorModelItem item = new OrchestratorModelItem();
			item.name = launchFileName;
			item.absolutePath = ros2LaunchFileDir + "/" + launchFileName + ros2launchFileExtension1;
			item.type = HandleType.ROS2_LAUNCHFILE;
			item.isStarted = false;
			item.onStart = false;
			item.debugPort = debugPort++;
			item.jmxPort = jmxPort;
			item.killMe = killMe++;
			jmxPort += 2;
			model.add(item);
		}

		for (String launchFileName : hzlaunchFileNames) {
			if(model.containsItemWithName(launchFileName)) {
				continue;
			}
			
			OrchestratorModelItem item = new OrchestratorModelItem();
			item.name = launchFileName;
			item.absolutePath = hzLaunchFileDir + "/" + launchFileName + hzlaunchFileExtension1;
			item.type = HandleType.HZ_LAUNCHFILE;
			item.isStarted = false;
			item.onStart = false;
			item.debugPort = debugPort++;
			item.jmxPort = jmxPort;
			item.killMe = killMe++;
			jmxPort += 2;
			model.add(item);
		}

		for (String launchFileName : jy2launchFileNames) {
			if(model.containsItemWithName(launchFileName)) {
				continue;
			}
			
			OrchestratorModelItem item = new OrchestratorModelItem();
			item.name = launchFileName;
			item.absolutePath = jy2LaunchFileDir + "/" + launchFileName + jy2launchFileExtension1;
			item.type = HandleType.JY2_LAUNCHFILE;
			item.isStarted = false;
			item.onStart = false;
			item.debugPort = debugPort++;
			item.jmxPort = jmxPort;
			item.killMe = killMe++;
			jmxPort += 2;
			model.add(item);
		}

		for (String jarFileName : jarFileNames) {
			if(model.containsItemWithName(jarFileName)) {
				continue;
			}
			
			OrchestratorModelItem item = new OrchestratorModelItem();
			item.name = jarFileName;
			item.absolutePath = jarFileDir + "/" + jarFileName + jarFileExtension1;
			item.type = HandleType.JARFILE;
			item.isStarted = false;
			item.onStart = false;
			item.debugPort = debugPort++;
			item.jmxPort = jmxPort;
			item.killMe = killMe++;
			jmxPort += 2;
			model.add(item);
		}

		for (String bashFileName : bashFileNames) {
			if(model.containsItemWithName(bashFileName)) {
				continue;
			}
			
			OrchestratorModelItem item = new OrchestratorModelItem();
			item.name = bashFileName;
			item.absolutePath = bashFileDir + "/" + bashFileName + bashFileExtension1;
			item.type = HandleType.BASHFILE;
			item.isStarted = false;
			item.onStart = false;
			item.debugPort = debugPort++;
			item.jmxPort = jmxPort;
			item.killMe = killMe++;
			jmxPort += 2;
			model.add(item);
		}

		model.nextDebugPort = debugPort;
		model.nextJmxPort = jmxPort;
		model.nextKillMe = killMe;
	}

}
