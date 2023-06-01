package com.github.jy2.example;

import java.util.Random;

import com.github.core.JyroscopeCore;
import com.github.jy2.MasterClient;

public class MainMaster {

	public static void main(String[] args) throws InterruptedException {
//		RosTypeConverters.scanAnnotationsAndInitialize();
		JyroscopeCore jy2 = new JyroscopeCore();
		jy2.addRemoteMaster("http://localhost:11311", "localhost", "/jy2" + new Random().nextInt());

		MasterClient core = jy2.getMasterClient();

		System.out.println(core.lookupNode("/rosout"));
		System.out.println(core.getTopicTypes());
		System.out.println(core.getSystemState());
	}

}
