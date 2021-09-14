package com.github.jy2.internal;

import com.github.jy2.Subscriber;

public interface DeleteSubscriber {
	
	<D> void deleteSubscriber(Subscriber<D> obj);

}
