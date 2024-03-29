package com.github.jy2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.github.jy2.di.LogSeldom;
import com.github.jy2.internal.DeleteSubscriber;
import com.github.jy2.log.Jy2DiLog;
import com.github.jy2.log.NodeNameManager;
import com.github.jy2.log.RosoutHandler;
import com.github.jy2.log.RosoutPublisher;
import com.github.jy2.mapper.RosTypeConverters;
import com.github.jy2.serialization.RosTypeConvertersSerializationWrapper;
import com.jyroscope.Link;
import com.jyroscope.local.Topic;
import com.jyroscope.local.TopicProvider;
import com.jyroscope.ros.RosTopicProvider;
import com.jyroscope.types.ConversionException;
import com.jyroscope.types.LinkManager;

import go.jyroscope.ros.jy2_msgs.JavaObject;

public class JyroscopeCore implements PubSubClient {

	private static final int SHUTDOWN_TIMEOUT = 5;

	public static RosoutPublisher ROSOUT_PUBLISHER;

	private ArrayList<TopicProvider<?>> providers = new ArrayList<>();

	public JyroscopeCore() {
		Runtime.getRuntime().addShutdownHook(new Thread("ShutdownHook") {
			public void run() {
				shutdown();
			}
		});
		try {
			RosTypeConverters.precompile(JavaObject.class);
		} catch (ConversionException e) {
			throw new RuntimeException("Cannot find ros type for java type " + JavaObject.class.getName(), e);
		}
	}

	public void addRemoteMaster(String masterUri, String localhostname, String callerId) {
		// TODO: resolve multiple masters...
		RosTopicProvider topicProvider = new RosTopicProvider("ros", masterUri, localhostname, callerId);
		providers.add(topicProvider);

//		// check if such node exists in ROS
//		if(topicProvider.getMasterClient().lookupNode(callerId)!=null) {
//			// if it exists - check if it's alive
//			// if dead - kill the other node
//			// if exists - do not allow running another node with such name
//			// throw new RuntimeException("Node already exist");
//		}

		configureLogging(topicProvider);

		configureUseThreadPools(topicProvider);
		
		// TODO: should create it only once...
		ROSOUT_PUBLISHER = new RosoutPublisher(this);
		NodeNameManager.setNodeName(callerId);
	}
	
	private void configureUseThreadPools(RosTopicProvider topicProvider) {
		boolean useThreadPoolListeners = false;
		Object param = null;
		try {
			param = topicProvider.getParameterClient().getParameter("/use_thread_pool_listeners");
			if (param != null) {
				useThreadPoolListeners = Boolean.parseBoolean(param.toString());
			}
		} catch (IOException e1) {
		}
		LinkManager.USE_THREADED_CONSUMER = !useThreadPoolListeners;

		boolean useThreadPoolRepeaters = false;
		try {
			param = topicProvider.getParameterClient().getParameter("/use_thread_pool_repeaters");
			if (param != null) {
				useThreadPoolRepeaters = Boolean.parseBoolean(param.toString());
			}
		} catch (IOException e1) {
		}
		LinkManager.USE_THREADED_REPEATER = !useThreadPoolRepeaters;

		int size = 500;
		try {
			param = topicProvider.getParameterClient().getParameter("/thread_pool_max_size");
			if (param != null) {
				size = Integer.parseInt(param.toString());
			}
		} catch (Exception e) {
		}
		LinkManager.WORK_QUEUE_MAX_SIZE = size;

		int buffer = 20;
		try {
			param = topicProvider.getParameterClient().getParameter("/thread_pool_buffer_size");
			if (param != null) {
				buffer = Integer.parseInt(param.toString());
			}
		} catch (Exception e) {
		}
		LinkManager.WORK_QUEUE_BUFFER_SIZE = buffer;

		size = 2;
		try {
			param = topicProvider.getParameterClient().getParameter("/scheduler_pool_size");
			if (param != null) {
				size = Integer.parseInt(param.toString());
			}
		} catch (Exception e) {
		}
		LinkManager.SCHEDULER_POOL_SIZE = size;
	}

	private void configureLogging(RosTopicProvider topicProvider) {
		Object param = null;
		try {
			param = topicProvider.getParameterClient().getParameter("/logging");
		} catch (IOException e1) {
			throw new RuntimeException("Unable to get parameter /logging", e1);
		}
		String configuration;
		if (param != null) {
			configuration = param.toString();
		} else {
			// Set jyroscope logging level to severe by default
			configuration = "com.jyroscope.level=SEVERE";
		}
		InputStream stream = new ByteArrayInputStream(configuration.getBytes());
		try {
			LogManager.getLogManager().readConfiguration(stream);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		Logger logger = Logger.getLogger("");
// leave logging handlers as configured by the user
//		Handler[] handlers = logger.getHandlers();
//		for (Handler h : handlers) {
//			logger.removeHandler(h);
//		}
		logger.addHandler(new ConsoleHandler());
		logger.addHandler(new RosoutHandler());
	}

	@Override
	public <D> Publisher<D> createPublisher(String topicName, Class<D> topicType, boolean latched, int queueSize,
			boolean lazy, int maxPublishingInterval) {
		try {
			RosTypeConvertersSerializationWrapper.precompile(topicType);
		} catch (ConversionException e) {
			throw new RuntimeException("Cannot find ros type for topic " + topicName + ", type " + topicType.getName(),
					e);
		}
		Topic<?> topic = getTopic(topicName);
		topic.setSendQueueSize(queueSize);
		if (lazy) {
			return new LazyJy2Publisher<D>(topic, topicType, latched, maxPublishingInterval);
		} else {
			return new Jy2Publisher<D>(topic, topicType, latched, maxPublishingInterval);
		}
	}

	@Override
	public <D> Subscriber<D> createSubscriber(String topicName, Class<D> topicType, int queueSize, int maxExecutionTime,
			boolean isReliable) {
		return createSubscriber(topicName, topicType, queueSize, maxExecutionTime, isReliable, null);
	}

	public <D> Subscriber<D> createSubscriber(String topicName, Class<D> topicType, int queueSize, int maxExecutionTime,
			boolean isReliable, DeleteSubscriber ds) {
		try {
			if (topicType != null) {
				RosTypeConvertersSerializationWrapper.precompile(topicType);
			}
		} catch (ConversionException e) {
			throw new RuntimeException("Cannot find ros type for topic " + topicName + ", type " + topicType.getName(),
					e);
		}
		return new Jy2Subscriber<D>(getTopic(topicName), topicType, ds);
	}

	@Override
	public ParameterClient getParameterClient() {
		// TODO: resolve multiple masters...
		for (TopicProvider<?> provider : providers) {
			return provider.getParameterClient();
		}
		// No provider found
		throw new IllegalStateException("Could not find topic provider");
	}

	@Override
	public MasterClient getMasterClient() {
		// TODO: resolve multiple masters...
		for (TopicProvider<?> provider : providers) {
			return provider.getMasterClient();
		}
		// No provider found
		throw new IllegalStateException("Could not find topic provider");
	}

	@Override
	public SlaveClient getSlaveClient(String name) {
		// TODO: resolve multiple masters...
		for (TopicProvider<?> provider : providers) {
			return provider.getSlaveClient(name);
		}
		// No provider found
		throw new IllegalStateException("Could not find topic provider");
	}

	@Override
	public void shutdown() {
		ExecutorService service = Executors.newFixedThreadPool(100);
		for (TopicProvider<?> p : providers) {
			p.shutdown(service);
		}
		service.shutdown();
		try {
			service.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		}
	}

	private Topic<?> getTopic(String name) {
//        for (TopicProvider<?> provider : providers) {
//            String prefix = provider.getPrefix();
//            if (name.startsWith(prefix)) {
//                name = name.substring(prefix.length());
//                return provider.getTopic(name);
//            }
//        }
		// TODO: resolve multiple masters...
		for (TopicProvider<?> provider : providers) {
			return provider.getTopic(name);
		}
		// No provider found
		throw new IllegalArgumentException("Could not find an appropriate topic provider for: " + name);
	}

	private final static class Jy2Publisher<D> implements Publisher<D> {

		private LogSeldom log;

		private final Topic<?> topic;
		private final Link<D> link;
		private final int maxPublishingInterval;
		private long lastPublishMilliseconds = 0;

		private Jy2Publisher(Topic<?> topic, Class<D> topicType, boolean latched, int maxPublishingInterval) {
			this.topic = topic;
			this.maxPublishingInterval = maxPublishingInterval;
			try {
				link = topic.getPublisher(topicType, latched);
			} catch (ConversionException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void publish(D message) {
			if (maxPublishingInterval > 0) {
				checkPublishingInterval();
			}
			link.handle(message);
		}

		@Override
		public int getNumberOfMessageListeners() {
			return topic.getNumberOfMessageListeners();
		}

		@Override
		public void skipLocalMessages(boolean skip) {
			topic.skipLocalMessages(skip);
		}

		private void checkPublishingInterval() {
			long time = System.currentTimeMillis();
			if (lastPublishMilliseconds == 0) {
				lastPublishMilliseconds = time;
			} else {
				long dt = time - lastPublishMilliseconds;
				lastPublishMilliseconds = time;
				if (dt > maxPublishingInterval) {
					if (log == null) {
						log = JyroscopeCore.getLog();
					}
					log.warn("Maximum publishing interval exceeded " + dt + " on topic " + topic.getName());
				}
			}
		}
	}

	private final static class LazyJy2Publisher<D> implements Publisher<D> {

		private LogSeldom log;

		private final Topic<?> topic;
		private final Class<D> topicType;
		private final boolean latched;
		private Link<D> link = null;
		private final int maxPublishingInterval;
		private long lastPublishMilliseconds = 0;

		private LazyJy2Publisher(Topic<?> topic, Class<D> topicType, boolean latched, int maxPublishingInterval) {
			this.topic = topic;
			this.topicType = topicType;
			this.latched = latched;
			this.maxPublishingInterval = maxPublishingInterval;
		}

		@Override
		public void publish(D message) {
			if (maxPublishingInterval > 0) {
				checkPublishingInterval();
			}
			if (link == null) {
				try {
					link = topic.getPublisher(topicType, latched);
				} catch (ConversionException e) {
					throw new RuntimeException(e);
				}
			}
			link.handle(message);
		}

		@Override
		public int getNumberOfMessageListeners() {
			return topic.getNumberOfMessageListeners();
		}

		@Override
		public void skipLocalMessages(boolean skip) {
			topic.skipLocalMessages(skip);
		}

		private void checkPublishingInterval() {
			long time = System.currentTimeMillis();
			if (lastPublishMilliseconds == 0) {
				lastPublishMilliseconds = time;
			} else {
				long dt = time - lastPublishMilliseconds;
				lastPublishMilliseconds = time;
				if (dt > maxPublishingInterval) {
					if (log == null) {
						log = JyroscopeCore.getLog();
					}
					log.warn("Maximum publishing interval exceeded " + dt + " on topic " + topic.getName());
				}
			}
		}
	}

	private final static class Jy2Subscriber<D> implements Subscriber<D> {

		private LogSeldom log = JyroscopeCore.getLog();

		private Topic<?> topic;
		private final Class<D> topicType;
		private ArrayList<LinkImplementation> links = new ArrayList<>();
		private DeleteSubscriber deleteSubscriber;

		private Jy2Subscriber(Topic<?> topic, Class<D> topicType, DeleteSubscriber ds) {
			this.topic = topic;
			this.topicType = topicType;
			this.deleteSubscriber = ds;
		}

		@Override
		public synchronized Object addMessageListener(Consumer<D> consumer, int queueLength, int timeout,
				int maxExecutionTime, boolean logStoppedReceivingMessage, Method method) {
			LinkImplementation link = new LinkImplementation(consumer);
			link.timeout = timeout;
			link.maxExecutionTime = maxExecutionTime;
			link.logStoppedReceivingMessage = logStoppedReceivingMessage;
			link.method = method;
			try {
				topic.subscribe((Link) link, queueLength, timeout);
			} catch (ConversionException e) {
				throw new RuntimeException(e);
			}
			links.add(link);
			return link;
		}

		@Override
		public synchronized void removeMessageListener(Object handle) {
			LinkImplementation link = (LinkImplementation) handle;
			link.enabled = false;
			topic.unsubscribe((Link) link);
			links.remove(link);
		}

		@Override
		public synchronized void removeAllMessageListeners() {
			for (LinkImplementation s : links) {
				s.enabled = false;
				topic.unsubscribe((Link) s);
			}
			links.clear();
		}

		@Override
		public boolean isLatched() {
			return topic.isLatched() || topic.isRemoteLatched();
		}

		@Override
		public String getRemoteJavaType() {
			return topic.getRemoteJavaType();
		}

		@Override
		public String getRemoteRosType() {
			return topic.getRemoteRosType();
		}

		private final class LinkImplementation implements Link<D> {
			public boolean logStoppedReceivingMessage;
			public int timeout;
			private final Consumer<D> consumer;
			private Thread timeoutThread;
			public int maxExecutionTime;
			public Method method;
			public boolean enabled = true;
			public boolean firstTimeWarning = false;

			private LinkImplementation(Consumer<D> consumer) {
				this.consumer = consumer;
			}

			@Override
			public Class<? extends D> getType() {
				return topicType;
			}

			@Override
			public void handle(Object message) {
				long before = System.currentTimeMillis();
				try {
					if (message == null) {
						if (timeout <= 0) {
							if (method != null) {
								log.error("Should not happen! Received null when timeout is " + timeout + " on topic "
										+ topic.getName() + ", in method " + method.toGenericString());

							} else {
								log.error("Should not happen! Received null when timeout is " + timeout + " on topic "
										+ topic.getName() + ", in consumer " + consumer.getClass().getName());
							}
						}
						if (firstTimeWarning && logStoppedReceivingMessage) {
							if (method != null) {
								log.info("Stopped receiving message on topic " + topic.getName() + ", in method "
										+ method.toGenericString());

							} else {
								log.info("Stopped receiving message on topic " + topic.getName() + ", in consumer "
										+ consumer.getClass().getName());
							}
						}
						firstTimeWarning = false;
					} else {
						firstTimeWarning = true;
					}
					consumer.accept((D) message);
				} catch (Exception e) {
					if (method != null) {
						log.error("Exception caught while handling message in method " + method.toGenericString()
								+ ", message: " + message, e);
					} else {
						log.error("Exception caught while handling message in consumer " + consumer.getClass().getName()
								+ ", message: " + message, e);
					}
				}
				long delta = System.currentTimeMillis() - before;
				if (delta > maxExecutionTime && maxExecutionTime > 0) {
					if (method != null) {
						log.warn("Subscriber execution time " + delta + " exceeded threshold " + maxExecutionTime
								+ " in method " + method.toGenericString() + ", message: " + message);
					} else {
						log.warn("Subscriber execution time " + delta + " exceeded threshold " + maxExecutionTime
								+ " in consumer " + consumer.getClass().getName() + ", message: " + message);
					}
				}
			}

			@Override
			public void setRemoteAttributes(boolean isLatched, String remoteRosType, String remoteJavaType) {
				throw new UnsupportedOperationException("Reserved for class Receive in LinkManager");
			}

			@Override
			public String getThreadName() {
				if (method != null) {
					return "Listener-" + topic.getName() + "-" + method.toString();
					// method.getDeclaringClass().getTypeName() + "." + method.getName();
				} else {
					return "Listener-" + topic.getName() + "-" + consumer.getClass().getName();
				}
			}
		}

		@Override
		public void shutdown() {
			removeAllMessageListeners();
			if (deleteSubscriber != null) {
				deleteSubscriber.deleteSubscriber(this);
			}
		}
	}

	/**
	 * Returns logger wrapped with /rosout log publisher.
	 */
	public static LogSeldom getLog(String name) {
		return new Jy2DiLog(name);
	}

	/**
	 * Returns logger wrapped with /rosout log publisher.
	 */
	public static LogSeldom getLog(Class<?> clazz) {
		return new Jy2DiLog(clazz);
	}

	/**
	 * Returns logger wrapped with /rosout log publisher that gets caller class
	 * name.
	 */
	public static LogSeldom getLog() {
		Throwable dummyException = new Throwable();
		StackTraceElement[] locations = dummyException.getStackTrace();
		String cname = "unknown";
		if (locations != null && locations.length > 1) {
			StackTraceElement caller = locations[1];
			cname = caller.getClassName();
		}
		return new Jy2DiLog(cname);
	}

}
