package com.github.jy2.di.example;

import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.di.exceptions.CreationException;

public class MainSubscriber {

	public static void main(String[] args) throws CreationException {

		JyroscopeDi hzDi = new JyroscopeDi("jy2_di_example_sub", args);
		hzDi.create(DemoSubscriber.class);
		hzDi.start();
	}

}
