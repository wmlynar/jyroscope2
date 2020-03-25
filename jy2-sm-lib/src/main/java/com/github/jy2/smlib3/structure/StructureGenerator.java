package com.github.jy2.smlib3.structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.di.LogSeldom;
import com.github.jy2.smlib.messages.SmStructure;
import com.github.jy2.smlib.messages.StateTransition;
import com.github.jy2.smlib3.State;
import com.github.jy2.smlib3.annotations.Transitions;

public class StructureGenerator {

	public static final LogSeldom LOG = JyroscopeDi.getLog();

	public SmStructure getStructure(State<?, ?> initialState) {

		SmStructure smStructure = new SmStructure();
		smStructure.setStart(initialState.getClass().getSimpleName());

		HashSet<Class<?>> visited = new HashSet<>();
		LinkedList<Class<?>> toVisit = new LinkedList<>();
		toVisit.add(initialState.getClass());

		ArrayList<StateTransition> stateTransitions = new ArrayList<>();
		while (true) {
			if (toVisit.isEmpty()) {
				break;
			}
			Class<?> c = toVisit.removeFirst();
			if (visited.contains(c)) {
				continue;
			}
			visited.add(c);
			StateTransition transition = new StateTransition();
			transition.setFrom(c.getSimpleName());

			ArrayList<String> toStates = new ArrayList<>();
			ArrayList<Class<?>> nextStates = getTransitions(c);
			for (Class<?> possibleNextState : nextStates) {
				toStates.add(possibleNextState.getSimpleName());
				toVisit.addLast(possibleNextState);
			}
			transition.setTo(toStates);
			stateTransitions.add(transition);

		}

		smStructure.setTransitions(stateTransitions);
		return smStructure;
	}

	public ArrayList<Class<?>> getTransitions(Class<?> clazz) {
		ArrayList<Class<?>> transitions = new ArrayList<>();
		Transitions t = clazz.getAnnotation(Transitions.class);
		if (t == null) {
			LOG.warn("No transitions annotation for state: " + clazz.getSimpleName());
		} else {
			for (Class<?> c : t.value()) {
				if (!c.equals(clazz)) {
					transitions.add(c);
				}
			}
		}
		return transitions;
	}
}
