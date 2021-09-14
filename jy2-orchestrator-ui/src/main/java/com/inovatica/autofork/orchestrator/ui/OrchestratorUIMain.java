package com.inovatica.autofork.orchestrator.ui;

import java.util.Random;

import com.github.jy2.di.JyroscopeDi;

public class OrchestratorUIMain {

	public static void main(String[] args) throws Exception {
		JyroscopeDi di = new JyroscopeDi("orchestrator_ui_" + Math.abs(new Random().nextInt()), args);
		DistributedInterface distributedInterface = di.create(DistributedInterface.class);
		ApplicationFrame applicationFrame = new ApplicationFrame();

		// wire
		distributedInterface.applicationFrame = applicationFrame;
		applicationFrame.distributedInterface = distributedInterface;

		// start
		di.start();
		applicationFrame.showFrame();

	}

}
