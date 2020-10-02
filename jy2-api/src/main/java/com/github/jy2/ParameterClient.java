package com.github.jy2;

import java.io.IOException;
import java.util.ArrayList;

public interface ParameterClient {

	boolean setParameter(String key, Object value) throws IOException;

	String getParameter(String key) throws IOException;

	boolean deleteParameter(String key) throws IOException;

	boolean hasParameter(String key) throws IOException;

	ArrayList<String> getParameterNames() throws IOException;

	Object addParameterListener(String key, ParameterListener listener) throws IOException;
	
	boolean removeParameterListener(Object id) throws IOException;

}