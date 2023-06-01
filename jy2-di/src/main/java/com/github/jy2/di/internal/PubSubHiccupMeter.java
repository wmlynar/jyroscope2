package com.github.jy2.di.internal;

import com.github.jy2.Publisher;
import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.api.LogSeldom;
import com.github.jy2.di.annotations.Parameter;
import com.github.jy2.di.annotations.Publish;
import com.github.jy2.di.annotations.Repeat;
import com.github.jy2.di.annotations.Subscribe;

public class PubSubHiccupMeter {

	private static final LogSeldom LOG = JyroscopeDi.getLog();

	@Publish("pubsubhiccup")
	Publisher<Long> longPublisher;

	@Parameter("/pubsub_hiccup_ms_to_log")
	private double minPubSubHiccupToLog = 5;

	private Publisher<Double> pubsubHiccupPublisher;

	@Repeat(interval = 10)
	public void repeat() {
		longPublisher.publish(System.nanoTime());
	}

	@Subscribe("pubsubhiccup")
	public void stringSubscription(Long msg) {
		long time = System.nanoTime() - msg;
		double timeMs = time * 0.000001;
		if (timeMs > minPubSubHiccupToLog) {
			LOG.warn("Pubsub hiccup: " + timeMs);
		}
		if(timeMs>1) {
			pubsubHiccupPublisher.publish(timeMs);					
		}
	}

//	public PubSubHiccupMeter(JyroscopeDi hzDi, String uuid) {
//		pubsubHiccupPublisher = hzDi.createPublisher("/hiccup/pubsub/" + uuid, double.class);
//	}

}
