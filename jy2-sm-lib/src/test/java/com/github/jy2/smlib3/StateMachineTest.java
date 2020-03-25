package com.github.jy2.smlib3;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.jy2.smlib3.StateMachine;

public class StateMachineTest {

	@Test
	public void test() {

		StringBuilder sb = new StringBuilder();

		TestInput input = new TestInput();
		TestOutput output = new TestOutput();
		StateMachine<TestInput, TestOutput> sm = new StateMachine<>(new TestState1(sb));

		sm.process(0, input, output);
		sm.process(0.5, input, output);
		sm.process(1, input, output);
		sm.process(1.5, input, output);
		sm.process(2, input, output);
		sm.process(2.5, input, output);

		System.out.println(sb.toString());

		assertEquals("next1(input,0.0,0.0)output1(input,output,0.0,0.0)next1(input,0.5,0.5)"
				+ "output1(input,output1,0.5,0.5)next1(input,1.0,1.0)output1(input,output1,1.0,1.0)"
				+ "next2(input,1.0,0.0)output2(input,output1,1.0,0.0)next2(input,1.5,0.5)"
				+ "output2(input,output2 defined in test state 1,1.5,0.5)next2(input,2.0,1.0)"
				+ "output2(input,output2 defined in test state 1,2.0,1.0)next1(input,2.0,0.0)"
				+ "output1(input,output2 defined in test state 1,2.0,0.0)next1(input,2.5,0.5)"
				+ "output1(input,output1,2.5,0.5)", sb.toString());
	}

}
