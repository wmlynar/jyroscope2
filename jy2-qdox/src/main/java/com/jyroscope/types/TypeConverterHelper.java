package com.jyroscope.types;

public interface TypeConverterHelper<S,D> {
    
    public String getReader(S from, D to);
    public String getWriter(D from, S to);

}
