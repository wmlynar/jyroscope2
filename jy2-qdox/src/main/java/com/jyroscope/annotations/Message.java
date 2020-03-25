package com.jyroscope.annotations;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Message {
    
    public String value() default "";
    
}
