package com.jyroscope.core.types;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.qdox.model.JavaType;

public class TypeConverterHelperCollection<S, D> implements TypeConverterHelper<S, JavaType> {
    
    private List<TypeConverterHelper> converters;
    
    public TypeConverterHelperCollection() {
        converters = new ArrayList<>();
    }
    
    public void add(TypeConverterHelper converter) {
        converters.add(converter);
    }

    @Override
	public String getReader(S from, JavaType to) {
        for (TypeConverterHelper converter : converters) {
            String reader = converter.getReader(from, to);
            if (reader != null)
				return reader;
		}
		return null;
	}

	@Override
	public String getWriter(JavaType type, S to) {
		for (TypeConverterHelper converter : converters) {
			String writer = converter.getWriter(type, to);
			if (writer != null)
				return writer;
		}
		return null;
	}

}
