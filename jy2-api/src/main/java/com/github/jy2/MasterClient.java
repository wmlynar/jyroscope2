package com.github.jy2;

import java.util.ArrayList;

public interface MasterClient {

	String lookupNode(String node);

	ArrayList<ArrayList<String>> getTopicTypes();

	ArrayList<ArrayList<ArrayList<Object>>> getSystemState();

}