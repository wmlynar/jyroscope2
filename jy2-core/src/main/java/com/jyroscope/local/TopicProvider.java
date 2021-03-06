package com.jyroscope.local;

import java.util.concurrent.ExecutorService;

import com.github.jy2.MasterClient;
import com.github.jy2.ParameterClient;
import com.github.jy2.SlaveClient;

public interface TopicProvider<T> {

    public String getPrefix();
    public Topic<T> getTopic(String name);
	public void shutdown(ExecutorService service);
	public ParameterClient getParameterClient();
	public MasterClient getMasterClient();
	public SlaveClient getSlaveClient(String name);
    
}
