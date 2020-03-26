package com.github.jy2.smlib.example.states;

import com.github.jy2.smlib.example.data.Input;
import com.github.jy2.smlib.example.data.Output;
import com.github.jy2.smlib3.Next;
import com.github.jy2.smlib3.State;
import com.github.jy2.smlib3.annotations.Transitions;

@Transitions({ State2.class })
public class State1 implements State<Input, Output> {
	
	@Override
	public Next next(Input input, double time, double duration) {
		if (duration > input.configuration.maxDurationOfState) {
			if((input.inputValue % 2) == 0) {
				return new Next(new State2(), "time passed, even value");
			} else {
				return new Next(new State3(), "time passed, odd value");
			}
		}
		return null;
	}

	@Override
	public void output(Input input, Output output, double time, double duration) {
		output.outputValue = 1;
	}
}
