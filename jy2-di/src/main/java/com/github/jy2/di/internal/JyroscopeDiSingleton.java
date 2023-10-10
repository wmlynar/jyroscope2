package com.github.jy2.di.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.function.Consumer;

import com.github.jy2.JyroscopeCore;
import com.github.jy2.Publisher;
import com.github.jy2.Subscriber;
import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.di.LogSeldom;
import com.github.jy2.di.annotations.Init;
import com.github.jy2.di.annotations.Parameter;
import com.github.jy2.di.annotations.Publish;
import com.github.jy2.di.annotations.Repeat;
import com.github.jy2.di.exceptions.CreationException;
import com.github.jy2.di.ros.TimeProvider;
import com.github.jy2.log.NodeNameManager;

import go.jyroscope.ros.introspection_msgs.Member;
import go.jyroscope.ros.introspection_msgs.Node;

public class JyroscopeDiSingleton {

	public static final TimeProvider TIME_PROVIDER = new TimeProvider();
	private final LogSeldom LOG = JyroscopeDi.getLog();

	public static JyroscopeCore jy2;

	private static JyroscopeDiSingleton singleton;
	private static boolean isShutdown;

	private static String memberName;
	private static ArrayList<JyroscopeDi> nodes = new ArrayList<JyroscopeDi>();

//	@Parameter("/use_memory_observer")
//	private boolean useMemoryObserver = false;

	@Parameter("/periodically_run_gc")
	private boolean periodicallyRunGc = true;

	@Parameter("/install_uncaught_exception_handler")
	private boolean installUncaughtExceptionHandler = true;

	@Parameter("/install_jvm_hiccup")
	private boolean installJvmHiccupMonitor = false;

	@Parameter("/install_pubsub_hiccup")
	private boolean installPubSubHiccupMonitor = false;

	@Parameter("/min_jvm_hiccup_to_log_ms")
	private double minJvmHiccupToLog = 5;

	@Parameter("/min_jvm_hiccup_to_publish_ms")
	private double minJvmHiccupToPublish = 0.1;

	@Parameter("/min_pubsub_hiccup_to_log_ms")
	private double minPubSubHiccupToLog = 5;

	@Parameter("/min_pubsub_hiccup_to_publish_ms")
	private double minPubSubHiccupToPublish = 0.5;

	@Publish("/introspection")
	private Publisher<Member> introspectionPublisher;

	public JyroscopeDiSingleton(HashMap<String, String> specialParameters, String name, JyroscopeDi jy2Di) {
		memberName = name;

		ExitProcessOnUncaughtException.memberName = memberName;

		String rosCrashlogFolder = System.getenv("ROS_CRASH_LOG_FOLDER");
		if (rosCrashlogFolder != null && !rosCrashlogFolder.isEmpty()) {
			ExitProcessOnUncaughtException.logFolder = rosCrashlogFolder;
		} else {
			System.out.println("ROS_CRASH_LOG_FOLDER environment variable not found");
		}

		// parse ip,hostname
		String host = "127.0.0.1";

		// parse master
		String master = "http://127.0.0.1:11311";

		String rosIp = System.getenv("ROS_IP");
		if (rosIp != null && !rosIp.isEmpty()) {
			host = rosIp;
		}
		String rosHostname = System.getenv("ROS_HOSTNAME");
		if (rosHostname != null && !rosHostname.isEmpty()) {
			host = rosHostname;
		}
		String rosMasterUri = System.getenv("ROS_MASTER_URI");
		if (rosMasterUri != null && !rosMasterUri.isEmpty()) {
			master = rosMasterUri;
		}
		InputStream is = null;
		Properties prop = null;
		String homeFolder = System.getProperty("user.home");
		if (homeFolder == null) {
			homeFolder = "/";
		}
		String fileName = homeFolder + "/ros.properties";
		try {
			prop = new Properties();
			is = new FileInputStream(new File(fileName));
			prop.load(is);
			System.out.println("Loading " + fileName);
			String propertyParameterValue = prop.getProperty("ros.ip");
			if (propertyParameterValue != null && !propertyParameterValue.isEmpty()) {
				host = propertyParameterValue;
			}
			propertyParameterValue = prop.getProperty("ros.hostname");
			if (propertyParameterValue != null && !propertyParameterValue.isEmpty()) {
				host = propertyParameterValue;
			}
			propertyParameterValue = prop.getProperty("ros.master.uri");
			if (propertyParameterValue != null) {
				master = propertyParameterValue;
			}
		} catch (FileNotFoundException e) {
			System.out.println(fileName + " not found");
		} catch (IOException e) {
			e.printStackTrace();
		}
		String specialParameterValue = specialParameters.get("ip");
		if (specialParameterValue != null && !specialParameterValue.isEmpty()) {
			host = specialParameterValue;
		}
		specialParameterValue = specialParameters.get("hostname");
		if (specialParameterValue != null && !specialParameterValue.isEmpty()) {
			host = specialParameterValue;
		}
		specialParameterValue = specialParameters.get("master");
		if (specialParameterValue != null) {
			master = specialParameterValue;
		}

		jy2 = new JyroscopeCore();
		jy2.addRemoteMaster(master, host, name);

		try {
			jy2Di.inject(this);
			jy2Di.inject(TIME_PROVIDER);
		} catch (CreationException e) {
			throw new RuntimeException(e);
		}
	}

	public static synchronized void initialize(HashMap<String, String> specialParameters, String name,
			JyroscopeDi jy2Di) {
		nodes.add(jy2Di);
		if (singleton != null) {
			return;
		}
		singleton = new JyroscopeDiSingleton(specialParameters, name, jy2Di);
	}

	public static synchronized void shutdown() {
		if (isShutdown) {
			return;
		}
		jy2.shutdown();
		isShutdown = true;
	}
	
	@Repeat(interval = 60 * 1000)
	public void runGc() {
		if (periodicallyRunGc) {
			System.gc();
		}
	}

	@Init
	public void init() {
		// start memory observer
//		if (useMemoryObserver) {
//			new MemoryObserver().start();
//		}

		if (installUncaughtExceptionHandler) {
			ExitProcessOnUncaughtException.register();
		}

		if (installJvmHiccupMonitor) {
//			Publisher<Double> jvmHiccupPublisher = hzDi.createPublisher("/hiccup/jvm/" + member.getUuid(),
//					double.class);
			// one common topic for collecting hiccups from all members
			Publisher<Double> jvmHiccupPublisher = jy2.createPublisher("/hiccup/jvm", Double.class);
			ThreadGroup tgb = new ThreadGroup(NodeNameManager.getNextThreadGroupName());
			new JvmHiccupMeterThread(tgb, value -> {
				double valueMs = value * 0.0000001;
				if (valueMs > minJvmHiccupToLog) {
					LOG.warn("Jvm hiccup: " + valueMs);
				}
				if (valueMs > minJvmHiccupToPublish) {
					jvmHiccupPublisher.publish(valueMs);
				}
			}, 5).start();
		}

		// TODO: will crash for client code
		if (installPubSubHiccupMonitor) {
//			Publisher<Double> pubsubHiccupPublisher = hzDi.createPublisher("/hiccup/pubsub/" + member.getUuid(),
//					double.class);
			// one common topic for collecting hiccups from all members
			Publisher<Double> pubsubHiccupPublisher = jy2.createPublisher("/hiccup/pubsub", Double.class);

			Publisher<Long> publisher = jy2.createPublisher("pubsubhiccup", Long.class);
			new Thread("PubSubHiccupThread") {
				@Override
				public void run() {
					try {
						while (true) {
							publisher.publish(System.nanoTime());
							Thread.sleep(10);
						}
					} catch (InterruptedException e) {
						LOG.info("Pub sub hiccup exiting");
					}
				};

			}.start();
			Subscriber<Long> ssubscriber = jy2.createSubscriber("pubsubhiccup", Long.class);
			ssubscriber.addMessageListener(new Consumer<Long>() {
				@Override
				public void accept(Long t) {
					long time = System.nanoTime() - t;
					double timeMs = time * 0.000001;
					if (timeMs > minPubSubHiccupToLog) {
						LOG.warn("Pubsub hiccup: " + timeMs);
					}
					if (timeMs > minPubSubHiccupToPublish) {
						pubsubHiccupPublisher.publish(timeMs);
					}
				}
			});
		}
	}

	@Repeat(interval = 1000)
	public void publishIntrospection() {
		long start = System.currentTimeMillis();
		try {
			if (introspectionPublisher.getNumberOfMessageListeners() < 1) {
				return;
			}
		} finally {
			long time = System.currentTimeMillis() - start;
			if (time > 100) {
				LOG.warn("getNumberOfMessageListeners execution time: " + time);
			}
		}
		Member m = new Member();
		m.name = memberName;
		int s = nodes.size();
		m.nodes = new Node[s];
		for (int i = 0; i < s; i++) {
			m.nodes[i] = new Node();
			JyroscopeDi di = nodes.get(i);
			m.nodes[i].name = di.getName();
			m.nodes[i].publishers = di.publishedTopics.toArray(new String[di.publishedTopics.size()]);
			m.nodes[i].subscribers = di.subscribedTopics.toArray(new String[di.subscribedTopics.size()]);
		}
		introspectionPublisher.publish(m);
	}

	public static Object getMemberName() {
		return memberName;
	}

}
