package com.github.core.internal;

import com.github.jy2.Subscriber;

public interface DeleteSubscriber {
	
	<D> void deleteSubscriber(Subscriber<D> obj);

}
