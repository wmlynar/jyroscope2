package com.jyroscope.local.types;

import com.jyroscope.types.*;

public class IdentityTypeConverter<S extends D,D> extends TypeConverter<S, D> {

    @Override
    public D convert(S source) {
        return source;
    }

}
