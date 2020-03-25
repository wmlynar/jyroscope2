package com.github.jy2.smlib3.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is only used for state graph generation. It defines possible
 * states to be returned by the <code>next()</code> method for an annotated
 * state. It should define an array of {@link Class} objects defining possible
 * next states.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Transitions {
	Class<?>[] value();
}
