package com.jyroscope.types;

public interface TypeConverterFactory {
    
    public <S,D> TypeConverter get(Class<? extends S> from, Class<? extends D> to) throws ConversionException;

}
