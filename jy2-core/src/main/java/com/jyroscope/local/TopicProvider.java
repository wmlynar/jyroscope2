package com.jyroscope.local;

import com.github.jy2.MasterClient;
import com.github.jy2.ParameterClient;

public interface TopicProvider<T> {

    public String getPrefix();
    public Topic<T> getTopic(String name);
	public void shutdown();
	public ParameterClient getParameterClient();
	public MasterClient getMasterClient();
    
}
