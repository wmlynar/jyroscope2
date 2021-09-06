package com.inovatica.orchestrator;

public interface OutputCallback {

	void logOutput(boolean isError, String item, String text);

}