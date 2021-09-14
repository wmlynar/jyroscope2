package com.github.jy2.di;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
//import java.lang.reflect.Parameter;
//import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.yaml.snakeyaml.Yaml;

import com.github.jy2.MasterClient;
import com.github.jy2.ParameterClient;
import com.github.jy2.ParameterListener;
import com.github.jy2.PubSubClient;
import com.github.jy2.Publisher;
import com.github.jy2.SlaveClient;
import com.github.jy2.Subscriber;
import com.github.jy2.di.annotations.Init;
import com.github.jy2.di.annotations.Inject;
import com.github.jy2.di.annotations.InstanceName;
import com.github.jy2.di.annotations.Publish;
import com.github.jy2.di.annotations.Repeat;
import com.github.jy2.di.annotations.RosTimeProvider;
import com.github.jy2.di.annotations.Subscribe;
import com.github.jy2.di.exceptions.CreationException;
import com.github.jy2.di.internal.ClassWithName;
import com.github.jy2.di.internal.GraphName;
import com.github.jy2.di.internal.Initializer;
import com.github.jy2.di.internal.InstanceWithName;
import com.github.jy2.di.internal.JyroscopeDiSingleton;
import com.github.jy2.di.internal.ParameterFromFileReference;
import com.github.jy2.di.internal.ParameterReference;
import com.github.jy2.di.internal.Repeater;
import com.github.jy2.di.internal.SubscriberRef;
import com.github.jy2.di.monitor.FileChangeMonitor;
import com.github.jy2.di.ros.TimeProvider;
import com.github.jy2.di.utils.JsonMapper;
import com.github.jy2.di.utils.YamlMapper;
import com.github.jy2.internal.DeleteSubscriber;
import com.github.jy2.log.Jy2DiLog;
import com.github.jy2.log.NodeNameManager;
import com.github.jy2.util.ExceptionUtil;

public class JyroscopeDi implements PubSubClient, DeleteSubscriber {

	private static final LogSeldom LOG = new Jy2DiLog(JyroscopeDi.class);

	private static final Yaml YAML = new Yaml();

	private HashMap<String, String> parameters = new HashMap<>();
	private HashMap<String, String> specialParameters = new HashMap<>();
	private HashMap<String, String> remappings = new HashMap<>();

	private ArrayList<Initializer> initializers = new ArrayList<>();
	private ArrayList<Repeater> repeaters = new ArrayList<>();
	private HashMap<InstanceWithName, Thread> repeatersMap = new HashMap<>();
	private ArrayList<SubscriberRef> subscriberRefs = new ArrayList<>();
	private ArrayList<ParameterReference> parameterReferences = new ArrayList<>();
	private HashMap<String, ParameterReference> parameterReferenceMap = new HashMap<>();
	private ArrayList<ParameterFromFileReference> parameterFromFileReferences = new ArrayList<>();

	private HashMap<ClassWithName, Object> instanceMap = new HashMap<>();
	private ArrayList<InstanceWithName> instancesToInjectList = new ArrayList<>();

	private ArrayList<Subscriber<?>> subscribers = new ArrayList<>();

	private JyroscopeDi parentSession;
	private ArrayList<JyroscopeDi> childSessions = new ArrayList<>();

	private String name;

	public HashSet<String> publishedTopics = new HashSet<>();
	public HashSet<String> subscribedTopics = new HashSet<>();

	private Object parameterListenerId;

	private FileChangeMonitor monitor;

	static {
		avoidSnakeyamlNpeUnderGraalVm();
	}

	private static void avoidSnakeyamlNpeUnderGraalVm() {
		// avoid snakeyaml NPE under graalvm in
		// org.yaml.snakeyaml.util.PlatformFeatureDetector.isRunningOnAndroid(PlatformFeatureDetector.java:25)
		if (System.getProperty("java.runtime.name") == null) {
			System.setProperty("java.runtime.name", "");
		}
	}

	public JyroscopeDi(String name, String[] args) throws CreationException {
		// parse special parameters
		for (int i = 0; i < args.length; i++) {
			if (!args[i].contains(":=")) {
				continue;
			}
			if (args[i].startsWith("__")) {
				int pos = args[i].indexOf(":=");
				String parameterName = args[i].substring(2, pos);
				String parameterValue = args[i].substring(pos + 2);
				specialParameters.put(parameterName, parameterValue);
			}
		}

		// parse name
		String specialParameterValue = specialParameters.get("name");
		if (specialParameterValue != null) {
			name = specialParameterValue;
		}
		this.name = graphNameOfName(name);

		// Needed by the loggers
		NodeNameManager.setNodeName(this.name);

		JyroscopeDiSingleton.initialize(specialParameters, this.name, this);

		// parse regular parameters and remappings
		for (int i = 0; i < args.length; i++) {
			if (!args[i].contains(":=")) {
				continue;
			}
			if (args[i].startsWith("_") && !args[i].startsWith("__")) {
				int pos = args[i].indexOf(":=");
				String parameterName = args[i].substring(1, pos);
				String parameterValue = args[i].substring(pos + 2);
				parameters.put(graphNameOfParameter("", parameterName), parameterValue);
			} else if (!args[i].startsWith("_")) {
				int pos = args[i].indexOf(":=");
				String remappingName = args[i].substring(0, pos);
				String remappingValue = args[i].substring(pos + 2);
				remappings.put(graphNameOfTopic("", remappingName), graphNameOfTopic("", remappingValue));
			}
		}

		// process parameters from command line
		for (Entry<String, String> parameter : parameters.entrySet()) {
			try {
				setParameter(parameter.getKey(), parameter.getValue());
			} catch (IOException e) {
				LOG.error("Unable to set parameter on server" + parameter.getKey() + " " + parameter.getValue(), e);
			}
		}
	}

	private JyroscopeDi(JyroscopeDi di) {
		parentSession = di;

		// copy from parent
		name = di.name;
		remappings = di.remappings;
	}

	public synchronized JyroscopeDi createChildSession() {
		JyroscopeDi di = new JyroscopeDi(this);
		childSessions.add(di);
		return di;
	}

	private void detach(JyroscopeDi di) {
		childSessions.remove(di);
	}

	public String getName() {
		return name;
	}

	public Object getMemberName() {
		return JyroscopeDiSingleton.getMemberName();
	}

	public TimeProvider getTimeProvider() {
		return JyroscopeDiSingleton.TIME_PROVIDER;
	}

	public synchronized void start() throws CreationException {

		// add jyroscope di instance
		instanceMap.put(new ClassWithName(this.getClass(), ""), this);
		instancesToInjectList.add(new InstanceWithName(this, ""));

		// inject dependencies
		injectDependencies();

		// get all the parameters
		for (ParameterReference ref : parameterReferences) {
			processParameterReference(ref);
		}

		// get all the parameters from file
		loadParametersFromFile();

		if (!parameterFromFileReferences.isEmpty()) {
			Subscriber<Boolean> subscriber = createSubscriber("reload_parameters", Boolean.class);
			Publisher<Boolean> publisher = createPublisher("parameters_reloaded", Boolean.class);
			subscriber.addMessageListener(msg -> {
				LOG.info("Reloading parameters from file");
				loadParametersFromFile();
				publisher.publish(true);
			});
			// install file monitor
			for (ParameterFromFileReference ref : parameterFromFileReferences) {
				if (ref.watch) {
					Object value = null;
					try {
						value = getParameter(ref.parameterName);
					} catch (IOException e1) {
						LOG.error("Exception while getting parameter " + ref.parameterName, e1);
					}
					if (value == null) {
						value = ref.defaultValue;
					}
					String fileName = value.toString();
					if (fileName.isEmpty()) {
						LOG.info("Empty parameter: " + ref.parameterName + ", skipping registering file monitor");
						continue;
					}
					if (monitor == null) {
						monitor = new FileChangeMonitor();
					}
					try {
						monitor.addFileListener(fileName, new Runnable() {
							@Override
							public void run() {
								LOG.info("Noticed file change, updating parameter from file");
								processParameterFromFileReference(ref);
								publisher.publish(true);
							}
						});
					} catch (IOException e) {
						LOG.error("Exception caught while adding file listener " + fileName, e);
					}
				}
			}
		}

		// add callback on parameter change
		registerParameterChangeCallback();

		// start all initializers
		for (Initializer initializer : initializers) {
			executeInitializer(initializer);
		}

		// start all repeaters
		for (Repeater repeater : repeaters) {
			startRepeater(repeater);
		}

		// register all the subscribers
		for (SubscriberRef subscriber : subscriberRefs) {
			subscriber.start();
		}
	}

	private synchronized void loadParametersFromFile() {
		for (ParameterFromFileReference ref : parameterFromFileReferences) {
			processParameterFromFileReference(ref);
		}
	}

	public synchronized void shutdown() {
		// detach from parent session
		if (parentSession != null) {
			parentSession.detach(this);
		}

		// clear initializers references
		initializers.clear();

		// shutdown all repeaters
		for (Repeater repeater : repeaters) {
			repeater.shutdown();
		}
		repeaters.clear();
		repeatersMap.clear();

		// shutdown all subscriber refs (created from annotations)
		for (SubscriberRef subscriber : subscriberRefs) {
			subscriber.shutdown();
		}
		subscriberRefs.clear();

		// shutdown all subscribers (created using api)
		for (Subscriber<?> subscriber : subscribers) {
			subscriber.removeAllMessageListeners();
		}
		subscribers.clear();

		// shutdown parameter references
		parameterReferences.clear();
		parameterReferenceMap.clear();

		// stop file monitor
		if (monitor != null) {
			monitor.removeAllListeners();
			monitor = null;
		}

		// remove parameter change callback
		if (parameterListenerId != null) {
			try {
				JyroscopeDiSingleton.jy2.getParameterClient().removeParameterListener(parameterListenerId);
			} catch (IOException e) {
				LOG.warn("Unable to remove parameter client", e);
			}
			parameterListenerId = null;
		}

		// clear instance references
		instanceMap.clear();
		instancesToInjectList.clear();

		// shutdown all child sessions
		for (JyroscopeDi session : childSessions) {
			session.shutdown();
		}
		childSessions.clear();
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

	public <T> T create(Class<T> clazz) throws CreationException {
		return create(clazz, "", true);
	}

	public <T> T inject(T object) throws CreationException {
		return inject(object, "", true);
	}

	public <T> T create(Class<T> clazz, boolean singleton) throws CreationException {
		return create(clazz, "", singleton);
	}

	public <T> T inject(T object, boolean singleton) throws CreationException {
		return inject(object, "", singleton);
	}

	public <T> T create(Class<T> clazz, String instanceName) throws CreationException {
		return create(clazz, instanceName, true);
	}

	public <T> T inject(T object, String instanceName) throws CreationException {
		return inject(object, instanceName, true);
	}

	/**
	 * Creates new object of the given class and injects the properties according to
	 * the annotations. Requires connectToRemoteMaster to be called before.
	 */
	public <T> T create(Class<T> clazz, String instanceName, boolean singleton) throws CreationException {
		try {
			return inject(clazz.newInstance(), instanceName, singleton);
		} catch (InstantiationException | IllegalAccessException e) {
			LOG.error("Could not create class " + clazz.toGenericString(), e);
			throw new CreationException("Could not create class " + clazz.toGenericString(), e);
		}
	}

	/**
	 * Injects the properties according to the annotations. Requires
	 * connectToRemoteMaster to be called before.
	 */
	public <T> T inject(T object, String instanceName, boolean singleton) throws CreationException {
		ClassWithName classWithName = new ClassWithName(object.getClass(), instanceName);
		Class<?> clazz = object.getClass();
		if (singleton) {
			// verify there is only one singleton
			JyroscopeDi di = this;
			while (di != null) {
				if (di.instanceMap.get(classWithName) != null) {
					throw new IllegalArgumentException("There can be only one singleton");
				}
				di = di.parentSession;
			}
		}
		try {
			// for each field
			for (Field field : clazz.getDeclaredFields()) {
				injectTimeProvider(field, object, clazz);
				injectInstanceName(field, object, instanceName);
				collectParameters(field, object, instanceName);
				collectParametersFromFile(field, object, instanceName);
				injectPublishers(field, object, instanceName);
			}

			// for each method
			for (Method method : clazz.getDeclaredMethods()) {
				collectInitializers(method, object);
				collectRepeaters(method, object);
				collectSubscriberRefs(method, object, instanceName);
			}

			// cache the instances for dependency injection
			if (singleton) {
				instanceMap.put(classWithName, object);
			}
			instancesToInjectList.add(new InstanceWithName(object, instanceName));
		} catch (IllegalAccessException e) {
			throw new CreationException("Exception while creating " + clazz.toString(), e);
		}
		return object;
	}

	public synchronized void wakeupRepeater(Object object, String name) {
		Thread thread = repeatersMap.get(new InstanceWithName(object, name));
		if (thread == null) {
			LOG.errorSeldom("Cannot wakeup repeater, cannot find " + object.getClass().getCanonicalName() + " " + name);
		} else {
			thread.interrupt();
		}
	}

	public <T> Publisher<T> createPublisher(String topicName, Class<T> topicType, boolean isLatched, int queueSize) {
		String newTopicName = graphNameOfTopic("", topicName);
		String remappedTopicName = remappings.get(newTopicName);
		if (remappedTopicName != null) {
			newTopicName = remappedTopicName;
		}
//		Class<?> type2 = singleton.topicTypeMap.get(topicName);
//		if (type2 == null) {
//			singleton.topicTypeMap.put(newTopicName, type);
//		} else if (!type.equals(type2)) {
//			throw new RuntimeException("Type mismatch in topic " + topicName + ", new type: " + type.getName()
//					+ ", existing type:" + type2.getName());
//		}
//
//		if (!JyroscopeDi.ALWAYS_USE_RELIABLE_TOPIC) {
//			IsReliable value = singleton.topicIsReliableMap.get(topicName);
//			boolean existingReliable = (value == IsReliable.TRUE);
//			if (value != null) {
//				if (isReliable != existingReliable) {
//					throw new RuntimeException("isReliable mismatch in topic " + topicName + ", new: " + isReliable
//							+ ", existing:" + existingReliable);
//				}
//			} else {
//				singleton.topicIsReliableMap.put(topicName, isReliable ? IsReliable.TRUE : IsReliable.FALSE);
//			}
//		}
//
//		singleton.topicsSet.add(newTopicName);
//		singleton.nodePublishersMap.put(this.name, topicName);
//		return new Publisher<>(singleton.hzInstance, singleton.latchedMap, newTopicName, false, isReliable);
		publishedTopics.add(topicName);
		return JyroscopeDiSingleton.jy2.createPublisher(newTopicName, topicType, isLatched, queueSize);
	}

	/**
	 * Type = null subscribes to any type.
	 */
	@Override
	public synchronized <T> Subscriber<T> createSubscriber(String topicName, Class<T> topicType, int queueLength,
			int maxExecutionTime, boolean isReliable) {
		String newTopicName = graphNameOfTopic("", topicName);
		String remappedTopicName = remappings.get(newTopicName);
		if (remappedTopicName != null) {
			newTopicName = remappedTopicName;
		}
//		// when type == null subscribe to any type (useful for hz tool)
//		if (type != null) {
//			Class<?> type2 = singleton.topicTypeMap.get(topicName);
//			if (type2 == null) {
//				singleton.topicTypeMap.put(newTopicName, type);
//			} else if (!type.equals(type2)) {
//				throw new RuntimeException("Type mismatch in topic " + topicName + ", new type: " + type.getName()
//						+ ", existing type:" + type2.getName());
//			}
//		}
//
//		if (!JyroscopeDi.ALWAYS_USE_RELIABLE_TOPIC) {
//			IsReliable value = singleton.topicIsReliableMap.get(topicName);
//			boolean existingReliable = (value == IsReliable.TRUE);
//			if (value != null) {
//				if (isReliable != existingReliable) {
//					throw new RuntimeException("isReliable mismatch in topic " + topicName + ", new: " + isReliable
//							+ ", existing:" + existingReliable);
//				}
//			} else {
//				singleton.topicIsReliableMap.put(topicName, isReliable ? IsReliable.TRUE : IsReliable.FALSE);
//			}
//		}
//
//		singleton.topicsSet.add(newTopicName);
//		singleton.nodeSubscribersMap.put(this.name, topicName);
//		return new Subscriber<>(singleton.hzInstance, singleton.latchedMap, newTopicName, queueLength, maxExecutionTime,
//				isReliable, LOG);
		subscribedTopics.add(topicName);
		Subscriber<T> subscriber = JyroscopeDiSingleton.jy2.createSubscriber(newTopicName, topicType, queueLength,
				maxExecutionTime, isReliable, this);
		subscribers.add(subscriber);
		return subscriber;
	}

	public synchronized <D> void deleteSubscriber(Subscriber<D> subscriber) {
		subscriber.removeAllMessageListeners();
		subscribers.remove(subscriber);
	}

	public <T> T getInstance(Class<T> type) {
		return getInstance(type, "", true);
	}

	public <T> T getInstance(Class<T> type, boolean singleton) {
		return getInstance(type, "", singleton);
	}

	@SuppressWarnings("unchecked")
	public <T> T getInstance(Class<T> type, String instanceName, boolean singleton) {
		try {
			ClassWithName c = new ClassWithName(type, instanceName);
			return (T) getInstance(c, singleton);
		} catch (CreationException e) {
			throw new RuntimeException(
					"Problem with creating instance: " + type.getCanonicalName() + ", instanceName: " + instanceName,
					e);
		}
	}

	public MasterClient getMasterClient() {
		return JyroscopeDiSingleton.jy2.getMasterClient();
	}

	public ParameterClient getParameterClient() {
		return JyroscopeDiSingleton.jy2.getParameterClient();
	}

	@Override
	public SlaveClient getSlaveClient(String name) {
		return JyroscopeDiSingleton.jy2.getSlaveClient(name);
	}

	private void executeInitializer(Initializer initializer) {
		makeAccessible(initializer.method);
		long before = System.currentTimeMillis();
		try {
			initializer.method.invoke(initializer.object);
		} catch (Exception e) {
			ExceptionUtil.rethrowErrorIfCauseIsError(e);
			LOG.error("Exception caught while calling node initializer " + initializer.method.toGenericString(), e);
		}
		long delta = System.currentTimeMillis() - before;
		if (delta > initializer.init.maxExecutionTime() && initializer.init.maxExecutionTime() > 0) {
			LOG.warn("Initializer execution time " + delta + " exceeded threshold "
					+ initializer.init.maxExecutionTime() + " in method " + initializer.method.toGenericString());
		}
	}

	private void startRepeater(Repeater repeater) {
		Object object = repeater.object;
		Method method = repeater.method;
		makeAccessible(method);
		Repeat repeat = repeater.repeat;
		boolean isDelay = repeat.delay() != 0;
		boolean isInterval = repeat.interval() != 0;
		repeater.thread = new Thread(new Runnable() {
			@Override
			public void run() {
				int count = 0;
				long start = System.currentTimeMillis();
				while ((repeat.count() == 0 || count < repeat.count()) && !repeater.shutdown) {
					count++;
					try {
						long before = System.currentTimeMillis();
						Object result = method.invoke(object);
						long delta = System.currentTimeMillis() - before;
						if (delta > repeater.repeat.maxExecutionTime() && repeater.repeat.maxExecutionTime() > 0) {
							LOG.warn("Repeater execution time " + delta + " exceeded threshold "
									+ repeater.repeat.maxExecutionTime() + " in method " + method.toGenericString());
						}

						// Check if it returned false
						if (result != null) {
							if (!(Boolean) result) {
								break;
							}
						}
					} catch (Exception e) {
						ExceptionUtil.rethrowErrorIfCauseIsError(e);
						LOG.error("Exception caught while calling repeater " + method.toGenericString(), e);
					}
					try {
						if (isDelay) {
							Thread.sleep(repeat.delay());
						} else if (isInterval) {
							long now = System.currentTimeMillis();
							long sleep = repeat.interval() - (now - start);
							start += repeat.interval();
							if (sleep > 0) {
								Thread.sleep(sleep);
							}
						}
					} catch (InterruptedException ie) {
						// thread was woken up, restart the counter
						start = System.currentTimeMillis();
					}
				}
			}
		}, "Repeater-" + method.toString());
		repeater.thread.start();
		String name = repeat.name();
		if (name != null && !name.isEmpty()) {
			repeatersMap.put(new InstanceWithName(repeater.object, name), repeater.thread);
		}
	}

	private <T> void injectPublishers(Field field, T object, String instanceName)
			throws IllegalAccessException, IllegalArgumentException, CreationException {
		Publish publish = field.getAnnotation(Publish.class);
		if (publish != null) {
			verifyNonStatic(field);
			makeAccessible(field);

			Publisher<?> publisher = createPublisher(publish, field, instanceName);
			field.set(object, publisher);
		}
	}

	private <T> void collectParametersFromFile(Field field, T object, String instanceName) {
		com.github.jy2.di.annotations.ParameterFromFile parameter = field
				.getAnnotation(com.github.jy2.di.annotations.ParameterFromFile.class);
		if (parameter != null) {
			makeAccessible(field);

			String parameterName = graphNameOfParameter(instanceName, parameter.name());
			ParameterFromFileReference ref = new ParameterFromFileReference(parameterName, parameter.defaultValue(),
					parameter.watch(), object, field);
			parameterFromFileReferences.add(ref);
		}
	}

	private <T> void collectParameters(Field field, T object, String instanceName)
			throws CreationException, IllegalAccessException {
		// inject parameters
		com.github.jy2.di.annotations.Parameter parameter = field
				.getAnnotation(com.github.jy2.di.annotations.Parameter.class);
		if (parameter != null) {
			verifyNonStatic(field);
			makeAccessible(field);

			String parameterName = graphNameOfParameter(instanceName, parameter.value());
			ParameterReference ref = new ParameterReference(parameterName, object, field);
			parameterReferences.add(ref);
			parameterReferenceMap.put(ref.parameterName, ref);
		}
	}

	private <T> void injectTimeProvider(Field field, T object, Class<?> clazz)
			throws IllegalAccessException, CreationException, IllegalArgumentException {
		// inject ros time provider
		RosTimeProvider rosLog = field.getAnnotation(RosTimeProvider.class);
		if (rosLog != null) {
			makeAccessible(field);
			field.set(object, JyroscopeDiSingleton.TIME_PROVIDER);
		}
	}

	private <T> void injectInstanceName(Field field, T object, String name)
			throws IllegalAccessException, IllegalArgumentException, CreationException {
		// inject instance name
		InstanceName instanceName = field.getAnnotation(InstanceName.class);
		if (instanceName != null) {
			makeAccessible(field);
			field.set(object, name);
		}
	}

	private void injectDependencies() throws CreationException {
		for (int i = 0; i < instancesToInjectList.size(); i++) {
			InstanceWithName object = instancesToInjectList.get(i);
			Class<?> clazz = object.instance.getClass();
			try {
				// for each field
				for (Field field : clazz.getDeclaredFields()) {
					Inject inject = field.getAnnotation(Inject.class);
					if (inject != null) {
						Class<?> type = field.getType();
						String instanceName = inject.instance();
						if (!instanceName.startsWith("/")) {
							if (object.name.isEmpty() || instanceName.isEmpty()) {
								instanceName = object.name + instanceName;
							} else {
								instanceName = object.name + "/" + instanceName;
							}
						}
						// when injecting RosJavaDi always use one instance
						if (type.equals(this.getClass())) {
							instanceName = "";
						}
						ClassWithName c = new ClassWithName(type, instanceName);
						boolean singleton = inject.singleton();
						Object instance = getInstance(c, singleton);
						makeAccessible(field);
						field.set(object.instance, instance);
					}
				}
			} catch (IllegalAccessException e) {
				throw new CreationException("Exception while injecting dependencies " + clazz.toString(), e);
			}
		}
		instancesToInjectList.clear();
	}

	private Object getInstance(ClassWithName c, boolean singleton) throws CreationException {
		if (singleton) {
			JyroscopeDi di = this;
			while (di != null) {
				Object object = di.instanceMap.get(c);
				if (object != null) {
					return object;
				}
				di = di.parentSession;
			}
		}
		if (!c.name.isEmpty()) {
			return create(c.type, c.name, singleton);
		} else {
			return create(c.type, singleton);
		}
	}

	private <T> void collectSubscriberRefs(Method method, T object, String instanceName) throws CreationException {
		// create subscribers
		Subscribe subscribe = method.getAnnotation(Subscribe.class);
		if (subscribe != null) {
			SubscriberRef subscriber = createSubscriberRef(subscribe, object, method, instanceName);
			subscriberRefs.add(subscriber);
		}
	}

	private <T> SubscriberRef createSubscriberRef(Subscribe subscribe, T object, Method method, String instanceName)
			throws CreationException {
		java.lang.reflect.Parameter[] parameters = method.getParameters();
		if (parameters.length != 1) {
			throw new CreationException(
					"Subscriber at " + method.toGenericString() + " must have exactly one parameter");
		}
		java.lang.reflect.Parameter parameter = parameters[0];
		final Class<?> type = parameter.getType();

		int queueLenght = subscribe.queueSize();
		int timeout = subscribe.timeout();
		int maxExecutionTime = subscribe.maxExecutionTime();

		String topicName = graphNameOfTopic(instanceName, subscribe.value());
		String remappedTopicName = remappings.get(topicName);
		if (remappedTopicName != null) {
			topicName = remappedTopicName;
		}

		// verify ros-correctness of name
		GraphName.verify(topicName);

//		Class<?> type2 = singleton.topicTypeMap.get(topicName);
//		if (type2 == null) {
//			singleton.topicTypeMap.put(topicName, type);
//		} else if (!type.equals(type2)) {
//			throw new RuntimeException("Type mismatch in topic " + topicName + ", new type: " + type.getName()
//					+ ", existing type: " + type2.getName());
//		}
//
//		if (!JyroscopeDi.ALWAYS_USE_RELIABLE_TOPIC) {
//			boolean newReliable = subscribe.reliable();
//			IsReliable value = singleton.topicIsReliableMap.get(topicName);
//			boolean existingReliable = (value == IsReliable.TRUE);
//			if (value != null) {
//				if (newReliable != existingReliable) {
//					throw new RuntimeException("isReliable mismatch in topic " + topicName + ", new: " + newReliable
//							+ ", existing:" + existingReliable);
//				}
//			} else {
//				singleton.topicIsReliableMap.put(topicName, newReliable ? IsReliable.TRUE : IsReliable.FALSE);
//			}
//		}
//
//		singleton.topicsSet.add(topicName);
//		singleton.nodeSubscribersMap.put(this.name, topicName);

		verifyNonStatic(method);
		makeAccessible(method);

		subscribedTopics.add(topicName);
		return new SubscriberRef(JyroscopeDiSingleton.jy2, object, method, topicName, type, queueLenght, timeout,
				maxExecutionTime, LOG);
	}

	private <T> void collectRepeaters(Method method, T object) throws CreationException {
		Repeat repeat = method.getAnnotation(Repeat.class);
		if (repeat != null) {
			verifyNonStatic(method);
			makeAccessible(method);
			repeaters.add(new Repeater(object, method, repeat));
		}
	}

	private <T> void collectInitializers(Method method, T object) throws CreationException {
		Init init = method.getAnnotation(Init.class);
		if (init != null) {
			verifyNonStatic(method);
			makeAccessible(method);
			initializers.add(new Initializer(object, method, init));
		}
	}

	private Publisher<?> createPublisher(Publish publish, Field field, String instanceName) {
		Type type = field.getGenericType();
		Type[] typeArgs = ((ParameterizedType) type).getActualTypeArguments();

		Class<?> topicType = getGenericParameterType(typeArgs[0]);
		if (topicType == null) {
			throw new UnsupportedClassVersionError(
					"Unrecognized type parameter for publisher at " + field.toGenericString());
		}

		boolean isLatched = publish.latched();

		String topicName = graphNameOfTopic(instanceName, publish.value());
		String remappedTopicName = remappings.get(topicName);
		if (remappedTopicName != null) {
			topicName = remappedTopicName;
		}

//		Class<?> type2 = singleton.topicTypeMap.get(topicName);
//		if (type2 == null) {
//			singleton.topicTypeMap.put(topicName, topicType);
//		} else if (!topicType.equals(type2)) {
//			throw new RuntimeException("Type mismatch in topic " + topicName + ", new type: " + topicType.getName()
//					+ ", existing type:" + type2.getName());
//		}
//
//		if (!JyroscopeDi.ALWAYS_USE_RELIABLE_TOPIC) {
//			boolean newReliable = publish.reliable();
//			IsReliable value = singleton.topicIsReliableMap.get(topicName);
//			boolean existingReliable = (value == IsReliable.TRUE);
//			if (value != null) {
//				if (newReliable != existingReliable) {
//					throw new RuntimeException("isReliable mismatch in topic " + topicName + ", new: " + newReliable
//							+ ", existing:" + existingReliable);
//				}
//			} else {
//				singleton.topicIsReliableMap.put(topicName, newReliable ? IsReliable.TRUE : IsReliable.FALSE);
//			}
//		}
//
//		singleton.topicsSet.add(topicName);
//		singleton.nodePublishersMap.put(this.name, topicName);

		publishedTopics.add(topicName);
		return JyroscopeDiSingleton.jy2.createPublisher(topicName, topicType, isLatched, publish.queueSize());
	}

	private Class<?> getGenericParameterType(Type param) {
		Class<?> topicType = null;
		if (param instanceof Class) {
			topicType = (Class<?>) param;
		} else if (param instanceof ParameterizedType) {
			Type rawType = ((ParameterizedType) param).getRawType();
			if (rawType instanceof Class) {
				topicType = (Class<?>) rawType;
			}
		}
		return topicType;
	}

	private void makeAccessible(Field field) {
		if (!Modifier.isPublic(field.getModifiers())) {
			field.setAccessible(true);
		}
	}

	private void makeAccessible(Method method) {
		if (!Modifier.isPublic(method.getModifiers())) {
			method.setAccessible(true);
		}
	}

	private void verifyNonStatic(Field field) throws CreationException {
		if (Modifier.isStatic(field.getModifiers())) {
			throw new CreationException("Field " + field.toGenericString() + " must be non-static");
		}
	}

	private void verifyNonStatic(Method method) throws CreationException {
		if (Modifier.isStatic(method.getModifiers())) {
			throw new CreationException("Method " + method.toGenericString() + " must be non-static");
		}
	}

	private String graphNameOfName(String name) {
		if (name.startsWith("/")) {
			return name;
		} else {
			return "/" + name;
		}
	}

	private String graphNameOfTopic(String instanceName, String name) {
		if (name.startsWith("/")) {
			return name;
		} else {
			if (instanceName.isEmpty()) {
				return "/" + this.name + "/" + name;
			} else {
				return "/" + this.name + "/" + instanceName + "/" + name;

			}
		}
	}

	private String graphNameOfParameter(String instanceName, String name) {
		if (!name.endsWith("/")) {
			name = name + "/";
		}
		if (name.startsWith("/")) {
			return name;
		} else {
			if (instanceName.isEmpty()) {
				return this.name + "/" + name;
			} else {
				return this.name + "/" + instanceName + "/" + name;

			}
		}
	}

	private void processParameterReference(ParameterReference ref) {
		try {
			Object value = getParameter(ref.parameterName);
			if (value == null) {
				LOG.info("Unset parameter: " + ref.parameterName + ", getting default value");
				publishParameter(ref.object, ref.field, ref.parameterName);
			} else {
				setParameterValueFromServer(ref.object, ref.field, ref.parameterName, value);
			}
		} catch (IOException e) {
			LOG.error("Unable to get parameter: " + ref.parameterName + ", getting default value");
		}
	}

	private void processParameterFromFileReference(ParameterFromFileReference ref) {
		Class<?> type = ref.field.getType();
		try {
			Object value = getParameter(ref.parameterName);
			if (value == null) {
				LOG.info("Unset parameter: " + ref.parameterName + ", getting default value: " + ref.defaultValue);
				value = ref.defaultValue;
			}

			String fileName = value.toString();
			if (fileName.isEmpty()) {
				LOG.info("Empty parameter: " + ref.parameterName + ", skipping reading parameter from file");
				return;
			}

			Object obj;
			if (fileName.endsWith(".json")) {
				// read json
				obj = JsonMapper.map(new File(fileName), type);
			} else {
				// read yaml
				obj = YamlMapper.map(new File(fileName), type);
			}
			ref.field.set(ref.object, obj);
		} catch (Exception e) {
			LOG.error("Cannot set parameter " + ref.field.getName() + " in " + ref.object.getClass().getCanonicalName()
					+ ", type " + type, e);
		}
	}

	private Object getParameter(String name) throws IOException {
		return JyroscopeDiSingleton.jy2.getParameterClient().getParameter(name);
	}

	private <T> void setParameter(String name, T value) throws IOException {
		JyroscopeDiSingleton.jy2.getParameterClient().setParameter(name, value);
	}

	private <T> void publishParameter(T object, Field field, String parameterName) {
		try {
			Object value = field.get(object);
			Class<?> type = field.getType();

			if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)) {
				if (value != null) {
					setParameter(parameterName, (boolean) value);
				} else {
					setParameter(parameterName, "");
				}
			} else if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)) {
				if (value != null) {
					setParameter(parameterName, (int) value);
				} else {
					setParameter(parameterName, "");
				}
			} else if (Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type)) {
				if (value != null) {
					setParameter(parameterName, (double) value);
				} else {
					setParameter(parameterName, "");
				}
			} else if (List.class.isAssignableFrom(type)) {
				if (value != null) {
					setParameter(parameterName, YAML.dump(value));
				} else {
					setParameter(parameterName, "[]");
				}
			} else if (Map.class.isAssignableFrom(type)) {
				if (value != null) {
					setParameter(parameterName, YAML.dump(value));
				} else {
					setParameter(parameterName, "{}");
				}
			} else { // if (String.class.isAssignableFrom(type)) {
				if (value != null) {
					setParameter(parameterName, value.toString());
				} else {
					setParameter(parameterName, "");
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException | IOException e) {
			LOG.info("Error publishing parameter value: " + parameterName, e);
		}
	}

	private <T> void setParameterValueFromServer(T object, Field field, String name, Object value) {
		Class<?> type = field.getType();
		try {
			if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)) {
				if (Boolean.class.isAssignableFrom(value.getClass())
						|| boolean.class.isAssignableFrom(value.getClass())) {
					field.set(object, (boolean) value);
				} else {
					field.set(object, Boolean.parseBoolean(value.toString()));
				}
			} else if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)) {
				// NOTE: bug was here. int a = (int)(Double)b does not compile, int a =
				// (int)(double)b does compile
				if (Integer.class.isAssignableFrom(value.getClass()) || int.class.isAssignableFrom(value.getClass())) {
					field.set(object, (int) value);
				} else if (Long.class.isAssignableFrom(value.getClass())
						|| long.class.isAssignableFrom(value.getClass())) {
					field.set(object, (int) (long) value);
				} else if (Double.class.isAssignableFrom(value.getClass())
						|| double.class.isAssignableFrom(value.getClass())) {
					field.set(object, (int) (double) value);
				} else {
					field.set(object, (int) Double.parseDouble(value.toString()));
				}
			} else if (Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type)) {
				if (Integer.class.isAssignableFrom(value.getClass()) || int.class.isAssignableFrom(value.getClass())
						|| Double.class.isAssignableFrom(value.getClass())
						|| double.class.isAssignableFrom(value.getClass())) {
					field.set(object, (double) value);
				} else {
					field.set(object, Double.parseDouble(value.toString()));
				}
			} else if (String.class.isAssignableFrom(type)) {
				field.set(object, value.toString());
			} else if (List.class.isAssignableFrom(type)) {
				field.set(object, YAML.loadAs(value.toString(), ArrayList.class));
			} else if (Map.class.isAssignableFrom(type)) {
				field.set(object, YAML.loadAs(value.toString(), HashMap.class));
			}
		} catch (NumberFormatException e) {
			LOG.error("Cannot set parameter " + field.getName() + " in " + object.getClass().getCanonicalName()
					+ ", wrong number format " + type + ", parameter: " + name + " " + value.toString(), e);
		} catch (IllegalArgumentException e) {
			LOG.error("Cannot set parameter " + field.getName() + " in " + object.getClass().getCanonicalName()
					+ ", incompatible types " + type + ", parameter: " + name + " " + value.toString()
					+ ", for exmple consider maing it a List not an ArrayList", e);
		} catch (ClassCastException e) {
			LOG.error("Cannot set parameter " + field.getName() + " in " + object.getClass().getCanonicalName()
					+ ", incompatible types " + type + ", parameter: " + name + " " + value.toString()
					+ ", for example consider maing it a List not an ArrayList", e);
		} catch (IllegalAccessException e) {
			LOG.error("Cannot set parameter " + field.getName() + " in " + object.getClass().getCanonicalName()
					+ ", illegal access " + type + ", parameter: " + name + " " + value.toString(), e);
		}

	}

	private void registerParameterChangeCallback() {
		try {
			parameterListenerId = JyroscopeDiSingleton.jy2.getParameterClient().addParameterListener("/",
					new ParameterListener() {
						@Override
						public void onParameterUpdated(String name, Object value) {
							onParameterChanged(name, value);
						}
					});
		} catch (IOException e) {
			LOG.error("Unable to register parameter change callback", e);
		}
	}

	private synchronized void onParameterChanged(String name, Object value) {
		LOG.debug("Parameter callback: " + name + ":=" + value.toString());
		ParameterReference ref = parameterReferenceMap.get(name);
		if (ref == null) {
			LOG.debug("Unknown parameter, skipping: " + name);
		} else {
			setParameterValueFromServer(ref.object, ref.field, name, value);
		}
	}

	public String getRemappedTopicName(String topicName) {
		String newTopicName = graphNameOfTopic("", topicName);
		String remappedTopicName = remappings.get(newTopicName);
		if (remappedTopicName != null) {
			newTopicName = remappedTopicName;
		}
		return newTopicName;
	}

	public String getCommandLineParameter(String parameterName) {
		if (parameters != null) {
			return parameters.get(graphNameOfParameter("", parameterName));
		} else {
			return null;
		}
	}

}
