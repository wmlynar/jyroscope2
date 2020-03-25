package com.github.jy2.smlib.example.states;

import com.github.jy2.smlib.example.data.Input;
import com.github.jy2.smlib.example.data.Output;
import com.github.jy2.smlib3.Next;
import com.github.jy2.smlib3.State;
import com.github.jy2.smlib3.annotations.Transitions;

@Transitions({ State1.class })
public class State3 implements State<Input, Output> {
	
	@Override
	public Next next(Input input, double time, double duration) {
		if (duration > input.configuration.maxDurationOfState) {
			return new Next(new State1(), "time passed");
		}
		return null;
	}

	@Override
	public void output(Input input, Output output, double time, double duration) {
		output.outputValue = 3;
	}
}
