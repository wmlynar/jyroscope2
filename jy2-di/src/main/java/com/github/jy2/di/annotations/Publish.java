package com.github.jy2.di.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Publish {
    
    String value();
	boolean latched() default false;
	boolean reliable() default false;
	int queueSize() default 5;
	boolean lazy() default false;
	int maxPublishingInterval() default 0;
    
}
