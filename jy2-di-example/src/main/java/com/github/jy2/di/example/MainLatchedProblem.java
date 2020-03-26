package com.github.jy2.di.example;

import java.io.IOException;

import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.di.exceptions.CreationException;
import com.github.jy2.introspection.IntrospectionClient;

public class MainLatchedProblem {

	public static void main(String[] args) throws CreationException, IOException, InterruptedException {

		JyroscopeDi hzDi = new JyroscopeDi("jy2_lathed_di_example", args);
		hzDi.create(DemoMap1.class);
		hzDi.start();
		
		Thread.sleep(1000);
		
		JyroscopeDi session = hzDi.createChildSession();
		session.create(DemoMap2.class);
		session.start();
	}

}
