package com.jyroscope.types;

import java.util.*;

public class TypeConverterHelperCollection<S,D> implements TypeConverterHelper<S,D> {
    
    private List<TypeConverterHelper> converters;
    
    public TypeConverterHelperCollection() {
        converters = new ArrayList<>();
    }
    
    public void add(TypeConverterHelper converter) {
        converters.add(converter);
    }

    @Override
    public String getReader(S from, D to) {
        for (TypeConverterHelper converter : converters) {
            String reader = converter.getReader(from, to);
            if (reader != null)
				return reader;
		}
		return null;
	}

	@Override
	public String getWriter(D from, S to) {
		for (TypeConverterHelper converter : converters) {
			String writer = converter.getWriter(from, to);
			if (writer != null)
				return writer;
		}
		return null;
	}

}
