package com.github.jy2.smlib3;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.annotation.Nonnull;

/**
 * Data object encoding next state to change to and the reason of state change.
 *
 */
public class Next {

	/**
	 * Object or {@link Class} defining next state.
	 */
	private State<?, ?> nextState;

	/**
	 * Reason of state change
	 */
	private String reason;

	/**
	 * Level on which to log the transition.
	 */
	private TransitionLevel level;

	/**
	 * Constructor used when one has to stay in the same state for some reason that
	 * needs explanation.
	 *
	 * @param reason The reason of state change.
	 */
	public Next(@Nonnull String reason) {
		this.nextState = null;
		this.reason = reason;
		this.level = TransitionLevel.DEFAULT;
	}

	public Next(@Nonnull String reason, TransitionLevel level) {
		this.nextState = null;
		this.reason = reason;
		this.level = level;
	}

	/**
	 * Constructor used when changing to a new state.
	 *
	 * @param nextState Instance of new state to be changed to.
	 * @param reason    The reason of state change.
	 */
	public Next(@Nonnull State<?, ?> nextState, @Nonnull String reason) {
		this.nextState = nextState;
		this.reason = reason;
		this.level = TransitionLevel.INFO;
	}

	public Next(@Nonnull State<?, ?> nextState, @Nonnull String reason, Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		throwable.printStackTrace(printWriter);

		this.nextState = nextState;
		this.reason = reason + '\n' + stringWriter.toString();
		this.level = TransitionLevel.INFO;
	}

	public Next(@Nonnull State<?, ?> nextState, @Nonnull String reason, TransitionLevel level) {
		this.nextState = nextState;
		this.reason = reason;
		this.level = level;
	}

	public Next(@Nonnull State<?, ?> nextState, @Nonnull String reason, Throwable throwable, TransitionLevel level) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		throwable.printStackTrace(printWriter);

		this.nextState = nextState;
		this.reason = reason + '\n' + stringWriter.toString();
		this.level = level;
	}

	/**
	 * @return returns true if state stays the same, but needs explanation why.
	 */
	public boolean isSameState() {
		return nextState == null;
	}

	/**
	 * @return {@link Class} or state instance of next state to change to or
	 *         <code>null</code> if state stays the same.
	 */
	public State<?, ?> getState() {
		return nextState;
	}

	/**
	 * @return Reason of state change.
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * @return Reason of state change.
	 */
	public TransitionLevel getLevel() {
		return level;
	}
}
