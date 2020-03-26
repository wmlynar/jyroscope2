package com.github.jy2.smlib3.structure;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.github.jy2.smlib.messages.SmStructure;
import com.github.jy2.smlib.messages.StateTransition;
import com.github.jy2.smlib3.structure.StructureGenerator;

public class StructureGeneratorTest {

	private StructureGenerator structureGenerator;

	@Before
	public void before() throws IllegalArgumentException, IllegalAccessException {

		structureGenerator = new StructureGenerator();
	}

	@Test
	public void testTransitions() {
		ArrayList<Class<?>> transitions = new ArrayList<>();

		transitions.clear();
		transitions.add(TestState2.class);
		assertEquals(transitions, structureGenerator.getTransitions(TestState1.class));

		transitions.clear();
		transitions.add(TestState1.class);
		transitions.add(TestState3.class);
		assertEquals(transitions, structureGenerator.getTransitions(TestState2.class));

		transitions.clear();
		transitions.add(TestState1.class);
		assertEquals(transitions, structureGenerator.getTransitions(TestState3.class));
	}

	@Test
	public void testStructure() {
		ArrayList<String> expectedTo = new ArrayList<>();
		SmStructure structure = structureGenerator.getStructure(new TestState2());

		assertEquals("TestState2", structure.getStart());

		ArrayList<StateTransition> transitions = structure.getTransitions();
		assertEquals(3, transitions.size());

		assertEquals("TestState2", transitions.get(0).getFrom());
		expectedTo.clear();
		expectedTo.add("TestState1");
		expectedTo.add("TestState3");
		assertEquals(expectedTo, transitions.get(0).getTo());

		assertEquals("TestState1", transitions.get(1).getFrom());
		expectedTo.clear();
		expectedTo.add("TestState2");
		assertEquals(expectedTo, transitions.get(1).getTo());

		assertEquals("TestState3", transitions.get(2).getFrom());
		expectedTo.clear();
		expectedTo.add("TestState1");
		assertEquals(expectedTo, transitions.get(2).getTo());
	}

}
