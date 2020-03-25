package com.github.jy2.sm.monitor;

import java.util.Random;

import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.sm.monitor.ros.RosHandler;
import com.github.jy2.sm.monitor.ui.ApplicationFrame;

public class SmLibMonitorMain {

	public static void main(String[] args) throws Exception {

		JyroscopeDi JyroscopeDi = new JyroscopeDi("sm_monitor_" + Math.abs(new Random().nextInt()), args);

		// creation of objects
		RosHandler rosHandler = JyroscopeDi.create(RosHandler.class);
		ApplicationFrame applicationFrame = JyroscopeDi.create(ApplicationFrame.class);

		// wiring the objects
		rosHandler.applicationFrame = applicationFrame;
		applicationFrame.rosHandler = rosHandler;

		// starting objects
		applicationFrame.showFrame();
		JyroscopeDi.start();

	}

}
