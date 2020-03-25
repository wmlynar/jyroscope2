package com.github.jy2.di.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ParameterFromFile {

    String name();

	String defaultValue() default "";
	
	boolean watch() default false;

}
