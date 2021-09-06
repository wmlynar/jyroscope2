package com.inovatica.orchestrator.internal;

import java.io.File;

import org.apache.commons.logging.Log;

import com.github.jy2.di.JyroscopeDi;
import com.inovatica.orchestrator.OutputCallback;
import com.inovatica.orchestrator.json.OrcherstratorStatus;
import com.inovatica.orchestrator.json.OrchestratorStatusItem;
import com.inovatica.orchestrator.model.OrchestratorModel;
import com.inovatica.orchestrator.model.OrchestratorModelItem;

public class OrchestratorStartStop {

	public static Log LOG = JyroscopeDi.getLog(OrchestratorStartStop.class);

	public boolean suspendDebug = false;
	public boolean remoteProfiling = false;
	public int newRatio;
	public boolean runAsSudoWhenSuffix;
	public boolean limitMemoryWhenXmx;
	public boolean useLegacyDebug;
	public boolean zGc;
	public int javaMemoryLimit;
	public String user;

	public OrchestratorModel model;

	public static Object monitor = new Object();

	private OutputCallback callback;

	public OrchestratorStartStop(OrchestratorModel model, OutputCallback callback) {
		this.model = model;
		this.callback = callback;
	}

	public void onStartup() {
		for (OrchestratorModelItem item : model.items) {
			if (item.onStart) {
				start(item.name);
			}
		}
	}

	public boolean start(String name) {
		synchronized (monitor) {
			LOG.info("Received request to start item: " + name);

			OrchestratorModelItem item = model.map.get(name);
			if (item == null) {
				LOG.error("Unknown item " + name);
				return false;
			}

			if (item.handle != null && item.isStarted) {
				LOG.error("Item already started " + item.name);
				return false;
			}

			String fileName = item.absolutePath;
			File file = new File(fileName);
			if (!file.exists()) {
				LOG.error("Cannot find file " + item.name + ", " + file.getAbsolutePath());
				return false;
			}
			LaunchHandle handle = new LaunchHandle(item, model.jarParams, model.javaOpts, model.debug, model.jmx,
					model.bashParams, model.hostName, model.heapDumpOnOutOfMemomry, model.heapDumpPath,
					model.shenandoahGc, model.concurrentGc, model.killOnOutOfMemory, newRatio, user,
					runAsSudoWhenSuffix, limitMemoryWhenXmx, callback);
			if (handle.start(item.type, item.name, item.absolutePath, model.workingDir, suspendDebug, remoteProfiling,
					useLegacyDebug, zGc, javaMemoryLimit)) {
				item.handle = handle;
			}

			return true;
		}
	}

	public boolean stop(String name, boolean destroyForcibly) {
		synchronized (monitor) {
			LOG.info("Received request to stop item: " + name);

			OrchestratorModelItem item = model.map.get(name);
			if (item == null) {
				LOG.error("Unknown item " + name);
				return false;
			}

			if (item.handle == null) {
				LOG.error("Item not started, handle is null " + item.name);
				return false;
			}

			item.handle.stop(destroyForcibly);

			item.handle = null;

			return true;
		}
	}

	public boolean isStarted(String name) {
		synchronized (monitor) {
			return false;
		}
	}

	public OrcherstratorStatus getStatus() {
		synchronized (monitor) {
			OrcherstratorStatus status = new OrcherstratorStatus();
			for (OrchestratorModelItem i : model.items()) {
				OrchestratorStatusItem item = new OrchestratorStatusItem();
				item.name = i.name;
				item.type = i.type.toString();
				item.isStarted = i.isStarted;
				item.onStart = i.onStart;
				item.debugPort = i.debugPort;
				item.jmxPort = i.jmxPort;
				status.items.add(item);

			}
			return status;
		}
	}

}
