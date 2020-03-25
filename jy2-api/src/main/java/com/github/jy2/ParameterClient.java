package com.github.jy2;

import java.util.ArrayList;

public interface ParameterClient {

	boolean setParameter(String key, Object value);

	Object getParameter(String key);

	boolean deleteParameter(String key);

	boolean hasParameter(String key);

	ArrayList<String> getParameterNames();

	Object addParameterListener(String key, ParameterListener listener);
	
	boolean removeParameterListener(Object id);

}