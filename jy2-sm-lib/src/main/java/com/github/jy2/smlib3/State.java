package com.github.jy2.smlib3;

/**
 * Interface to be implemented by state object. <code>next</code> and
 * <code>output</code> methods will be called in each cycle of the state
 * machine.
 */
public interface State<Input, Output> {

	/**
	 * This function should compute what is the next state. It will always be called
	 * before the <code>output</code> method.
	 *
	 * @param input
	 *            Input object.
	 * @param time
	 *            Current system time.
	 * @param duration
	 *            Duration of current state
	 * @return Returns <code>null</code> if no state change, otherwise it should
	 *         return object of type {@link Next} pointing to next state and the
	 *         reason of state change.
	 */
	Next next(Input input, double time, double duration);

	/**
	 * This function should compute what is the output. It will always be called
	 * after the <code>next</code> function.
	 *
	 * @param input
	 *            Input object.
	 * @param output
	 *            Output object.
	 * @param time
	 *            Current system time at the moment <code>next</code> method is
	 *            called.
	 * @param duration
	 *            Duration of current state.
	 */
	void output(Input input, Output output, double time, double duration);

}
