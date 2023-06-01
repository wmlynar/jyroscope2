package com.jyroscope.base.local.types;

import com.jyroscope.types.TypeConverter;

public class IdentityTypeConverter<S extends D,D> extends TypeConverter<S, D> {

    @Override
    public D convert(S source) {
        return source;
    }

}
