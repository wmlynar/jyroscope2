package com.jyroscope.types;

public abstract class TypeConverter<S,D> {

    public abstract D convert(S source);
        
}
