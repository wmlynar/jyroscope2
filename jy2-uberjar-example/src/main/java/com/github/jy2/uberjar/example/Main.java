package com.github.jy2.uberjar.example;

import com.github.jy2.uberjar.Util;
import com.github.jy2.ParameterClient;
import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.di.exceptions.CreationException;
import com.github.jy2.log.NodeNameManager;

public class Main {
	
	public static void main(String[] args) throws CreationException {
		
		JyroscopeDi jyDi = new JyroscopeDi("autofork_uberjar", args);
		ParameterClient pc = jyDi.getParameterClient();


		new Thread(new ThreadGroup("main"), new Runnable() {
			@Override
			public void run() {
				try {
					com.github.jy2.di.example.Main.main(new String[] {"__name:=jy2_di_example_launch"});
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}).start();

	}
}
