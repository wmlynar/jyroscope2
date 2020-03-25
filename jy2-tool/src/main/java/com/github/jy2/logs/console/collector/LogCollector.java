package com.github.jy2.logs.console.collector;

import com.github.jy2.di.annotations.Subscribe;
import com.github.jy2.logs.console.model.Model;

import go.jyroscope.ros.rosgraph_msgs.Log;

public class LogCollector {

	public Model model = new Model();

	@Subscribe("/rosout")
	public void handleLog(Log msg) {
		model.add(msg);
	}

}
