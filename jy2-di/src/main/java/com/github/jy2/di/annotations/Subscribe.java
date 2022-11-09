package com.github.jy2.di.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscribe {

	String value();

	int queueSize() default 5;

	int timeout() default 0;

	int maxExecutionTime() default 50;

	boolean reliable() default false;
	
	boolean logStoppedReceivingMessage() default true;
	
}
