package com.github.jy2.smlib.example;

import com.github.jy2.di.annotations.Init;
import com.github.jy2.di.annotations.Inject;
import com.github.jy2.di.annotations.Repeat;
import com.github.jy2.smlib.example.data.Configuration;
import com.github.jy2.smlib.example.data.Input;
import com.github.jy2.smlib.example.data.Output;
import com.github.jy2.smlib3.StateMachine;
import com.github.jy2.smlib3.utils.StatePublisher;

public class Example {
	
	@Inject
	Input input;
	
	@Inject
	Output output;
	
	@Inject
	Configuration configuration;
	
	@Inject(instance = "smlib_statemachine")
	StateMachine<Input, Output> stateMachine;
	
	@Repeat(interval = 100)
	void repeat() {
		input.inputValue++;
		double time = System.currentTimeMillis() * 0.001;
		stateMachine.process(time, input, output);
		System.out.println(output.outputValue);
	}

}
