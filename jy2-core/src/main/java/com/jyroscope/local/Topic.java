package com.jyroscope.local;

import com.jyroscope.Link;
import com.jyroscope.types.ConversionException;

public interface Topic<T> {
    
    void subscribe(Link<T> subscriber) throws ConversionException;
	void subscribe(Link<T> subscriber, int queueSize) throws ConversionException;
    void unsubscribe(Link<T> subscriber);
    <D> Link<D> getPublisher(Class<? extends D> type, boolean latched) throws ConversionException;
	boolean isLatched();
	boolean isRemoteLatched();
	String getRemoteJavaType();
	String getRemoteRosType();

	int getNumberOfMessageListeners();
	void skipLocalMessages(boolean skip);

}
