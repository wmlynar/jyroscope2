package com.jyroscope.local;

import com.github.jy2.MasterClient;
import com.jyroscope.Name;
import com.jyroscope.SystemException;
import com.jyroscope.ros.parameters.RosParameterClient;

public class LocalTopicProvider<T> implements TopicProvider<T> {

    private String prefix;
    private Name<LocalTopic> names;
    
    public LocalTopicProvider(String prefix) {
        names = new Name<>(name -> new LocalTopic(name));
        this.prefix = prefix + ":";
    }
    
    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public Topic<T> getTopic(String name) {
        try {
            return names.parse(name).get();
        } catch (SystemException se) {
            // This should never occur
            throw new RuntimeException(se);
        }
    }

	@Override
	public void shutdown() {
	}

	@Override
	public RosParameterClient getParameterClient() {
		return null;
	}

	@Override
	public MasterClient getMasterClient() {
		return null;
	}

}
