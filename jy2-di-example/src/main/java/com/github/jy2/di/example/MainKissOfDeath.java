package com.github.jy2.di.example;

import java.io.IOException;

import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.di.exceptions.CreationException;

public class MainKissOfDeath {

	public static void main(String[] args) throws CreationException, IOException {

		JyroscopeDi hzDi = new JyroscopeDi("jy2_di_example", args);
		hzDi.start();
		
		throw new OutOfMemoryError();
	}

}
