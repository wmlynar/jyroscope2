package com.github.jy2;

public interface PubSubClient {

	/**
	 * Type = null subscribes to any type.
	 */
	<D> Publisher<D> createPublisher(String topicName, Class<D> topicType, boolean latched);

	/**
	 * Type = null subscribes to any type.
	 */
	<D> Subscriber<D> createSubscriber(String topicName, Class<D> topicType, int queueSize, int maxExecutionTime,
			boolean isReliable);

	ParameterClient getParameterClient();

	MasterClient getMasterClient();

	SlaveClient getSlaveClient(String name);

	void shutdown();

	default <D> Publisher<D> createPublisher(String topicName, Class<D> topicType) {
		return createPublisher(topicName, topicType, false);
	}

	default <D> Subscriber<D> createSubscriber(String topicName, Class<D> topicType) {
		return createSubscriber(topicName, topicType, 5, 50, false);
	}

	default <D> Subscriber<D> createSubscriber(String topicName, Class<D> topicType, int queueSize) {
		return createSubscriber(topicName, topicType, queueSize, 50, false);
	}

	default <D> Subscriber<D> createSubscriber(String topicName, Class<D> topicType, int queueSize,
			int maxExecutionTime) {
		return createSubscriber(topicName, topicType, queueSize, maxExecutionTime, false);
	}
}
