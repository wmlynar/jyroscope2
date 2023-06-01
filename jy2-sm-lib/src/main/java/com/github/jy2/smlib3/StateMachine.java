package com.github.jy2.smlib3;

import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.api.LogSeldom;
import com.github.jy2.smlib.messages.SmState;
import com.github.jy2.smlib.messages.SmStructure;
import com.github.jy2.smlib3.serializer.Serializer;
import com.github.jy2.smlib3.structure.StructureGenerator;

/**
 * The purpose of this library is to set conventions. Imagine there is a
 * critical situation and you do don't know where to look for the source of the
 * problem. By following the conventions you make it easier for other people to
 * read your code and find problems. Please try to follow the conventions so we
 * can all debug problems easier. Thanks.
 */
public class StateMachine<Input, Output> {

	public final LogSeldom log;

	public static final int MAX_NEXT_STATE_ITERATIONS = 100;

	Serializer serializer = new Serializer();

	private State<Input, Output> startState;
	private State<Input, Output> currentState;
	private double timeOfStateChange;
	private boolean isInitialized;
	private double time;
	private double startTime;
	private TransitionLevel defaultTransitionLevel;

	private SmStructure structure;

	private Input lastInput = null;
	private Output lastOutput = null;

	public StateMachine(State<Input, Output> startState) {
		this.log = JyroscopeDi.getLog();
		this.startState = startState;
		this.structure = new StructureGenerator().getStructure(startState);
		this.defaultTransitionLevel = TransitionLevel.INFO;
	}

	public StateMachine(State<Input, Output> startState, TransitionLevel defaultTransitionLevel) {
		this.log = JyroscopeDi.getLog();
		this.startState = startState;
		this.structure = new StructureGenerator().getStructure(startState);
		if (defaultTransitionLevel == TransitionLevel.DEFAULT) {
			this.defaultTransitionLevel = TransitionLevel.INFO;
		} else {
			this.defaultTransitionLevel = defaultTransitionLevel;
		}
	}

	public StateMachine(State<Input, Output> startState, String loggingClass, TransitionLevel defaultTransitionLevel) {
		this.log = JyroscopeDi.getLog(loggingClass);
		this.startState = startState;
		this.structure = new StructureGenerator().getStructure(startState);
		if (defaultTransitionLevel == TransitionLevel.DEFAULT) {
			this.defaultTransitionLevel = TransitionLevel.INFO;
		} else {
			this.defaultTransitionLevel = defaultTransitionLevel;
		}
	}

	public StateMachine(State<Input, Output> startState, Class<?> loggingClass,
			TransitionLevel defaultTransitionLevel) {
		this.log = JyroscopeDi.getLog(loggingClass);
		this.startState = startState;
		this.structure = new StructureGenerator().getStructure(startState);
		if (defaultTransitionLevel == TransitionLevel.DEFAULT) {
			this.defaultTransitionLevel = TransitionLevel.INFO;
		} else {
			this.defaultTransitionLevel = defaultTransitionLevel;
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized void process(double time, Input input, Output output) {
		lastInput = input;
		lastOutput = output;
		if (time < this.time) {
			log.error(String.format("New time %.4f < old time %.4f", time, this.time));
		}
		if (!isInitialized) {
			currentState = startState;
			startTime = time;
			timeOfStateChange = time;
			isInitialized = true;

			logNewStateChange(startState, "start state", defaultTransitionLevel);
		}
		this.time = time;
		for (int i = 0; i < MAX_NEXT_STATE_ITERATIONS; i++) {
			double duration = time - timeOfStateChange;

			Next next = currentState.next(input, time, duration);
			currentState.output(input, output, time, duration);

			if (next == null) {
				return;
			}

			if (next.getState() == null) {
				String message = "Exceptional reason for not changing state : "
						+ currentState.getClass().getSimpleName() + ", reason: " + next.getReason();
				logSameStateChange(message, next.getLevel());
				return;
			}

			if (next.getState().getClass().equals(currentState.getClass())) {
				log.warnSeldom(
						"State.next() should return null when not changing state, state: " + getCurrentStateName());
				String message = "Exceptional reason for not changing state : "
						+ currentState.getClass().getSimpleName() + ", reason: " + next.getReason();
				logSameStateChange(message, next.getLevel());
				return;
			}

			State<Input, Output> nextState = null;
			try {
				nextState = (State<Input, Output>) next.getState();
			} catch (ClassCastException e) {
				log.errorSeldom("Wrong value returned in State.next(), class cast error, state: "
						+ getCurrentStateName() + ", next: " + next.getState().getClass().getSimpleName(), e);
				return;
			}
			currentState = nextState;
			timeOfStateChange = time;

			logNewStateChange(next.getState(), next.getReason(), next.getLevel());

		}
		log.errorSeldom("Statemachine next state looped, continuing but please react, state: " + getCurrentStateName());
	}

	@SuppressWarnings("rawtypes")
	public synchronized Class<? extends State> getCurrentState() {
		if (currentState == null) {
			return null;
		}
		return currentState.getClass();
	}

	public synchronized String getCurrentStateName() {
		if (currentState == null) {
			return "not_started";
		}
		return currentState.getClass().getSimpleName();
	}

	public synchronized SmStructure getSmStructure() {
		return structure;
	}

	public synchronized SmState getSmState() {
		SmState smState = new SmState();
		try {
			smState.setState(getCurrentStateName());
			if (lastInput != null) {
				smState.setInput(serializer.serialize(lastInput));
			} else {
				smState.setInput("");
			}
			if (lastOutput != null) {
				smState.setOutput(serializer.serialize(lastOutput));
			} else {
				smState.setOutput("");
			}
			if (currentState != null) {
				smState.setAttributes(serializer.serialize(currentState));
			} else {
				smState.setAttributes("");
			}
			smState.setConfig("");
			smState.setTime(time);
			smState.setLife(time - startTime);
			smState.setDuration(time - timeOfStateChange);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			log.errorSeldom("Problem serializing state information", e);
		}
		return smState;
	}

	private void logNewStateChange(State<?, ?> state, String reason, TransitionLevel level) {
		String serialized = "";
		try {
			serialized = serializer.serialize(state);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			log.errorSeldom("Exception caught while serializing state: " + getCurrentStateName(), e);
		}

		String message = "State change to : " + state.getClass().getSimpleName() + ", reason: " + reason;
		if (!serialized.isEmpty()) {
			message = message + ", attributes:\n" + serialized;
		}

		if (level == TransitionLevel.DEFAULT) {
			level = defaultTransitionLevel;
		}

		if (level == TransitionLevel.DEBUG) {
			log.debug(message);
		} else if (level == TransitionLevel.INFO) {
			log.info(message);
		} else if (level == TransitionLevel.WARNING) {
			log.warn(message);
		} else {
			log.error(message);
		}
	}

	private void logSameStateChange(String reason, TransitionLevel level) {
		if (level == TransitionLevel.DEFAULT) {
			level = defaultTransitionLevel;
		}

		if (level == TransitionLevel.DEBUG) {
			log.debug(reason);
		} else if (level == TransitionLevel.INFO) {
			log.infoSeldom(reason);
		} else if (level == TransitionLevel.WARNING) {
			log.warnSeldom(reason);
		} else {
			log.errorSeldom(reason);
		}
	}

}
