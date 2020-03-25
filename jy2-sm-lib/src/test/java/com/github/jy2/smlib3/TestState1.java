package com.github.jy2.smlib3;

import com.github.jy2.smlib3.Next;
import com.github.jy2.smlib3.State;
import com.github.jy2.smlib3.annotations.DontSerialize;
import com.github.jy2.smlib3.annotations.Transitions;

@Transitions({ TestState1.class, TestState2.class })
public class TestState1 implements State<TestInput, TestOutput> {

	@DontSerialize
	private StringBuilder sb;

	public TestState1(StringBuilder sb) {
		this.sb = sb;
	}

	@Override
	public Next next(TestInput input, double time, double duration) {
		sb.append("next1(");
		sb.append(input).append(",").append(time).append(",").append(duration);
		sb.append(")");

		if (duration > 0.9) {
			return new Next(new TestState2(sb, "defined in test state 1"), "changed to state 2");
		}
		return null;
	}

	@Override
	public void output(TestInput input, TestOutput output, double time, double duration) {
		sb.append("output1(");
		sb.append(input).append(",").append(output).append(",").append(time).append(",").append(duration);
		sb.append(")");

		output.message = "output1";
	}

}
