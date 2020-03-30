package com.github.jy2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.github.jy2.di.LogSeldom;
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

import go.jyroscope.ros.jy2_msgs.JavaObject;

public class JyroscopeCore implements PubSubClient {

	public static RosoutPublisher ROSOUT_PUBLISHER;

	private ArrayList<TopicProvider<?>> providers = new ArrayList<>();

	public JyroscopeCore() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
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

		// TODO: should create it only once...
		ROSOUT_PUBLISHER = new RosoutPublisher(this);
		NodeNameManager.setNodeName(callerId);
	}

	private void configureLogging(RosTopicProvider topicProvider) {
		Object param = topicProvider.getParameterClient().getParameter("/logging");
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
//		logger.addHandler(new ConsoleHandler());
		logger.addHandler(new RosoutHandler());
	}

	@Override
	public <D> Publisher<D> createPublisher(String topicName, Class<D> topicType, boolean latched) {
		try {
			RosTypeConvertersSerializationWrapper.precompile(topicType);
		} catch (ConversionException e) {
			throw new RuntimeException("Cannot find ros type for topic " + topicName + ", type " + topicType.getName(),
					e);
		}
		return new Jy2Publisher<D>(getTopic(topicName), topicType, latched);
	}

	@Override
	public <D> Subscriber<D> createSubscriber(String topicName, Class<D> topicType, int queueSize, int maxExecutionTime,
			boolean isReliable) {
		try {
			if (topicType != null) {
				RosTypeConvertersSerializationWrapper.precompile(topicType);
			}
		} catch (ConversionException e) {
			throw new RuntimeException("Cannot find ros type for topic " + topicName + ", type " + topicType.getName(),
					e);
		}
		return new Jy2Subscriber<D>(getTopic(topicName), topicType);
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
		for (TopicProvider<?> p : providers) {
			p.shutdown();
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
		private final Topic<?> topic;
		private final Link<D> link;

		private Jy2Publisher(Topic<?> topic, Class<D> topicType, boolean latched) {
			this.topic = topic;
			try {
				link = topic.getPublisher(topicType, latched);
			} catch (ConversionException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void publish(D message) {
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
	}

	private final static class Jy2Subscriber<D> implements Subscriber<D> {

		private LogSeldom log = JyroscopeCore.getLog();

		private Topic<?> topic;
		private final Class<D> topicType;
		private ArrayList<LinkImplementation> links = new ArrayList<>();

		private Jy2Subscriber(Topic<?> topic, Class<D> topicType) {
			this.topic = topic;
			this.topicType = topicType;
		}

		@Override
		public synchronized Object addMessageListener(Consumer<D> consumer, int queueLength, int timeout,
				int maxExecutionTime, Method method) {
			LinkImplementation link = new LinkImplementation(consumer);
			link.maxExecutionTime = maxExecutionTime;
			link.method = method;
			try {
				topic.subscribe((Link) link, queueLength);
			} catch (ConversionException e) {
				throw new RuntimeException(e);
			}

			links.add(link);

			if (timeout <= 0) {
				return link;
			}

			link.lastMessageTime = System.currentTimeMillis();
			link.timeoutThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (link.enabled) {
						long time = System.currentTimeMillis();
						long dt = time - link.lastMessageTime;
						if (dt < 0) {
							log.error(
									"Subscriber timeout: something wrong with time in the system (usually because of time synchronization), dt="
											+ dt);
							dt = 0;
						}
						if (dt >= timeout) {
							dt = 0;
							try {
								consumer.accept(null);
							} catch (Throwable e) {
								if (method != null) {
									log.error("Exception caught while handling message in method "
											+ method.toGenericString() + ", message: null", e);

								} else {
									log.error("Exception caught while handling handling message in consumer "
											+ consumer.getClass().getName() + ", message: null", e);
								}
							}
							long delta = System.currentTimeMillis() - time;
							if (delta > maxExecutionTime && maxExecutionTime > 0) {
								if (method != null) {
									log.warn("Subscriber execution time " + delta + " exceeded threshold "
											+ maxExecutionTime + " in method " + method.toGenericString()
											+ ", message: null");
								} else {
									log.warn("Subscriber execution time " + delta + " exceeded threshold "
											+ maxExecutionTime + " in consumer " + consumer.getClass().getName()
											+ ", message: null");
								}
							}
						}
						try {
							Thread.sleep(timeout - dt);
						} catch (InterruptedException e) {
							// do nothing
						}
					}
				}
			});
			link.timeoutThread.start();

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
			private final Consumer<D> consumer;
			private long lastMessageTime;
			private Thread timeoutThread;
			public int maxExecutionTime;
			public Method method;
			public boolean enabled = true;

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
				lastMessageTime = before;
				try {
					consumer.accept((D) message);
				} catch (Throwable e) {
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
