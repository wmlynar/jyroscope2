package com.github.jy2.smlib3.serializer;

import com.github.jy2.smlib3.annotations.DontSerialize;

public class TestObject {

	double value1 = 1.0;
	String value2 = "aa";
	TestEnum e = TestEnum.ONE;
	TestObject2 obj1 = null;
	TestObject2 obj2 = new TestObject2();

	@DontSerialize
	int value = 1;

}
