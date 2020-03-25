package com.github.jy2.smlib.example;

import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.di.exceptions.CreationException;
import com.github.jy2.smlib.example.data.Configuration;
import com.github.jy2.smlib.example.data.Input;
import com.github.jy2.smlib.example.data.Output;
import com.github.jy2.smlib.example.states.State1;
import com.github.jy2.smlib3.StateMachine;
import com.github.jy2.smlib3.utils.StatePublisher;

public class Main {

	public static void main(String[] args) throws CreationException {

		JyroscopeDi jyDi = new JyroscopeDi("jy_sm_lib_example", args);

		jyDi.create(Configuration.class);
		jyDi.create(Input.class);
		jyDi.create(Output.class);
		jyDi.create(Example.class);
		StateMachine<Input, Output> sm = jyDi.inject(new StateMachine<>(new State1()), "smlib_statemachine");

		StatePublisher.start(jyDi, sm, 250);
		
		jyDi.start();

	}
}
