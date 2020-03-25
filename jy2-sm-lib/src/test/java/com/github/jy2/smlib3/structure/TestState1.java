package com.github.jy2.smlib3.structure;

import com.github.jy2.smlib3.Next;
import com.github.jy2.smlib3.State;
import com.github.jy2.smlib3.annotations.Transitions;

@Transitions({ TestState1.class, TestState2.class })
public class TestState1 implements State<Object, Object> {

	@Override
	public Next next(Object input, double time, double duration) {
		return null;
	}

	@Override
	public void output(Object input, Object output, double time, double duration) {
	}

}
