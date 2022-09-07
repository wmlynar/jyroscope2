package com.github.jy2.di.example;

import java.io.IOException;

import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.di.exceptions.CreationException;
import com.github.jy2.introspection.IntrospectionClient;

public class Main {

	public static void main(String[] args) throws CreationException, IOException {

		JyroscopeDi hzDi = new JyroscopeDi("jy2_di_example", args);
//		hzDi.create(DemoPublisher.class);
//		hzDi.create(DemoSubscriber.class);
//		hzDi.create(DemoParameters.class);
//		hzDi.create(DemoIntrospection.class);
//		hzDi.inject(new IntrospectionClient(hzDi));
//		hzDi.create(DemoTimeProvider.class);
//		hzDi.create(DemoLogging.class);
		hzDi.create(DemoLazy.class);
		hzDi.start();
	}

}
