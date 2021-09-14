package com.inovatica.orchestrator.json;

import java.io.Serializable;

public class OrchestratorStatusItem implements Serializable {

	public String name;
	public String type;
	public boolean isStarted;
	public boolean onStart;
	public int debugPort;
	public int jmxPort;

}
