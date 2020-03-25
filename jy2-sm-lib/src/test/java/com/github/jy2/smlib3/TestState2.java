package com.github.jy2.smlib3;

import com.github.jy2.smlib3.Next;
import com.github.jy2.smlib3.State;
import com.github.jy2.smlib3.annotations.DontSerialize;
import com.github.jy2.smlib3.annotations.Transitions;

@Transitions({ TestState1.class, TestState2.class })
public class TestState2 implements State<TestInput, TestOutput> {

	@DontSerialize
	private StringBuilder sb;

	private String string;

	public TestState2(StringBuilder sb, String string) {
		this.sb = sb;
		this.string = string;
	}

	@Override
	public Next next(TestInput input, double time, double duration) {
		sb.append("next2(");
		sb.append(input).append(",").append(time).append(",").append(duration);
		sb.append(")");

		if (duration > 0.9) {
			return new Next(new TestState1(sb), "changed to state 1");
		}
		return null;
	}

	@Override
	public void output(TestInput input, TestOutput output, double time, double duration) {
		sb.append("output2(");
		sb.append(input).append(",").append(output).append(",").append(time).append(",").append(duration);
		sb.append(")");

		output.message = "output2 " + string;
	}

}
