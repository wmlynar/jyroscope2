package com.jyroscope.core.types;

import com.jyroscope.types.ConversionException;
import com.jyroscope.types.TypeConverter;

public interface TypeConverterFactory {
    
    public <S,D> TypeConverter get(Class<? extends S> from, Class<? extends D> to) throws ConversionException;

}
