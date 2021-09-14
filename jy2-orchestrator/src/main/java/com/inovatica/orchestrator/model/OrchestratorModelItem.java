package com.inovatica.orchestrator.model;

import com.inovatica.orchestrator.internal.HandleType;
import com.inovatica.orchestrator.internal.LaunchHandle;

//@SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
public class OrchestratorModelItem {

	// attributes
	public String name;
	public HandleType type;
	public String absolutePath;
	public boolean isStarted;
	public boolean onStart;
	public int debugPort;
	public int jmxPort;
	public int killMe;

	// handle
	public LaunchHandle handle;
}
