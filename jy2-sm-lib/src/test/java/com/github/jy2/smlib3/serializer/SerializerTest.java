package com.github.jy2.smlib3.serializer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.jy2.smlib3.serializer.Serializer;

public class SerializerTest {

	@Test
	public void test() throws IllegalArgumentException, IllegalAccessException {
		Serializer serializer = new Serializer();
		String str = serializer.serialize(new TestObject());
		assertEquals("value1=1.0\nvalue2=aa\ne=ONE\nobj1=null\nobj2:\n  intval=2", str);
	}

}
