package com.github.jy2.di.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Repeat {

    int delay() default 0;
    int interval() default 0;
    int count() default 0;
    int maxExecutionTime() default 50;
    String name() default "";
    
}
