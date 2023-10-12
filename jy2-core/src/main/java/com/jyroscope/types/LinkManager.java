package com.jyroscope.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import org.ros.concurrent.CircularBlockingDeque;

import com.github.jy2.di.LogSeldom;
import com.github.jy2.log.Jy2DiLog;
import com.github.jy2.log.NodeNameManager;
import com.github.jy2.mapper.RosTypeConverters;
import com.github.jy2.serialization.RosTypeConvertersSerializationWrapper;
import com.github.jy2.workqueue.MessageProcessor;
import com.github.jy2.workqueue.MessageProcessorFactory;
import com.jyroscope.Link;
import com.jyroscope.ros.RosMessage;

// TODO handle concurrent access
public class LinkManager {
	
	private final static LogSeldom LOG = new Jy2DiLog(LinkManager.class);

    private ReadWriteLock lock = new ReentrantReadWriteLock();

	private boolean sendLocalMessages = true;
    
    private class Receive<D> implements Link<D> {
        
        private final Class<? extends D> fromType;
        private final boolean isLocal;
        private volatile boolean isLatched;
        private volatile D latchedValue;
        
        // for latched topics and "*" subscriber
        private String remoteRosType;
        private String remoteJavaType;
        private Class remoteToType;
        
		private Receive(Class<? extends D> fromType, boolean isLocal, boolean isLatched) {
            this.fromType = fromType;
            this.isLocal = isLocal;
			this.isLatched = isLatched;
        }

        @Override
        public Class<? extends D> getType() {
            return fromType;
        }

        @Override
        public void handle(D message) {
			if (isLatched) {
	            lock.writeLock().lock();
	            try {
                    latchedValue = message;
	            } finally {
	                lock.writeLock().unlock();
	            }
			}
            process(message);
        }
        
        private void process(D message) {
            lock.readLock().lock();
            try {
                for (Map.Entry<Class<?>, Deliver<?>> entry : listeners.entrySet()) {
                    Deliver<?> deliver = entry.getValue();
                    if (deliver.hasClients(isLocal)) {
						try {
							Class<?> toType = entry.getKey();
							// WOJ: Handle case where subscriber wishes to accept all types: "*"
							// In that case to == null
							if (toType == null) {
								computeRemoteTypeWithoutLock();
								toType = remoteToType;
							}
//							TypeConverter converter = TypeConverter.get(fromType, entry.getKey());
							TypeConverter converter = RosTypeConvertersSerializationWrapper.get(fromType, toType);
							if (converter == null) {
								String toTypeStr = "null";
								if (toType != null) {
									toTypeStr = toType.getCanonicalName();
								}
								LOG.error("Unable to convert message from " + fromType.getCanonicalName() + " to "
										+ toTypeStr + ", remote ros type " + remoteRosType + ", remote java type  "
										+ remoteJavaType);
								return;
							}
							if(message instanceof RosMessage) {
								// make sure that when ros message is handled for the second time
								// the buffer will be at start position
								((RosMessage)message).reset();
							}
							Object converted = converter.convert(message);
							((Deliver) deliver).handle(converted, isLocal);
						} catch (Exception e) {
							LOG.error("Exception caught when handling message: " + message + ", type "
									+ fromType.getCanonicalName() + ", remote ros type " + remoteRosType
									+ ", remote java type  " + remoteJavaType, e);
						}
                    }
                }
            } finally {
                lock.readLock().unlock();
            }
        }

		@Override
		public void setRemoteAttributes(boolean isLatched, String remoteRosType, String remoteJavaType) {
			if("*".equals(remoteRosType)) {
				// skip connections to jy tool
				return;
			}
            lock.writeLock().lock();
            try {
				Receive.this.isLatched = isLatched;
				// needed when subscriber type is "*" and type=null
				Receive.this.remoteRosType = remoteRosType;
				Receive.this.remoteJavaType = remoteJavaType;
            } finally {
                lock.writeLock().unlock();
            }
		}

		// only called within link manager from locked code 
		public void computeRemoteTypeWithoutLock() {
			if (remoteToType != null) {
				return;
			}
			try {
				if (!fromType.equals(RosMessage.class)) {
					remoteToType = fromType;
				} else {
					RosTypeConverters.precompileByRosName(remoteRosType);
					remoteToType = RosTypeConverters.getRosType(remoteRosType);
					if (remoteJavaType != null) {
						ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
						if (classLoader != null) {
							remoteToType = classLoader.loadClass(remoteJavaType);
						} else {
							remoteToType = Class.forName(remoteJavaType);
						}
						RosTypeConvertersSerializationWrapper.precompile(remoteToType);						
					}
				}
			} catch (Exception e) {
				LOG.error("Exception caught when converting message: " + fromType + " " + remoteRosType + " " + remoteJavaType, e);
			}
		}

		@Override
		public String getThreadName() {
			throw new UnsupportedOperationException("No thread name");
		}
    }
    
	private class Deliver<D> {
        
//        private final Class<? extends D> to;
//        private final ArrayList<Link<D>> local;
        private final ArrayList<Link<D>> remote;
		private HashMap<Link<D>, WorkConsumer<D>> localConsumers;
//		private HashMap<Link<D>, Consumer<D>> remoteConsumers;

	    private ReadWriteLock lock = new ReentrantReadWriteLock();
		
        private Deliver(Class<? extends D> to) {
//            this.to = to;
//            this.local = new ArrayList<>();
            this.remote = new ArrayList<>();
			this.localConsumers = new HashMap<>();
//			this.remoteConsumers = new HashMap<>();
        }
        
		private void add(Link<D> link, boolean isLocal, int queueSize, int timeout) {
			lock.writeLock().lock();
			try {
            if (isLocal) {
            	WorkConsumer<D> consumer;
				if (USE_THREADED_CONSUMER) {
					consumer = new ThreadedConsumer<>(link, queueSize, timeout);
				} else {
					consumer = new WorkQueueConsumer<>(link, queueSize, timeout);
				}
//                local.add(link);
				localConsumers.put(link, consumer);
            }
            else
                remote.add(link);
//				remoteConsumers.put(link, consumer);
			} finally {
				lock.writeLock().unlock();
			}
        }
        
        private void remove(Link<D> link) {
			lock.writeLock().lock();
			try {
//            local.remove(link);
            remote.remove(link);
			WorkConsumer<D> consumer = localConsumers.remove(link);
			if (consumer != null) {
				consumer.stop();
			}
//			consumer = remoteConsumers.remove(link);
//			if (consumer != null) {
//				consumer.stop();
//			}
			} finally {
				lock.writeLock().unlock();
			}
        }
        
        private boolean hasLocalClients() {
			lock.readLock().lock();
			try {
//            return local.size() > 0;
			return localConsumers.size() > 0;
			} finally {
				lock.readLock().unlock();
			}
        }
        
        private boolean hasClients(boolean forMessageFromLocal) {
			lock.readLock().lock();
			try {
//            return local.size() > 0 || (forMessageFromLocal && remote.size() > 0);
//			return localConsumers.size() > 0 || (forMessageFromLocal && remoteConsumers.size() > 0);
			return localConsumers.size() > 0 || (forMessageFromLocal && remote.size() > 0);
			} finally {
				lock.readLock().unlock();
			}
        }
        
        private void handle(D message, boolean isLocal) {
			lock.readLock().lock();
			try {
//            if (local.size() > 0)
//                for (Link<D> link : local)
//                    link.handle(message);
//            if (isLocal && remote.size() > 0)
//                for (Link<D> link : remote)
//                    link.handle(message);
        	// woj: when receiving message from remote isLocal=false
        	// when accepting message from same vm isLocal = true
        	// out logic is as follows
        	// * if remote accept all: !isLocal
        	// * if local accept only if sendLocalMessages is true: sendLocalMessages 
			if (localConsumers.size() > 0 && (!isLocal | sendLocalMessages))
				for (WorkConsumer<D> consumer : localConsumers.values())
					consumer.offer(message);
//			if (isLocal && remoteConsumers.size() > 0)
//				for (Consumer<D> consumer : remoteConsumers.values())
//					consumer.offer(message);
			if (isLocal && remote.size() > 0)
				for (Link<D> link : remote)
					link.handle(message);
			} finally {
				lock.readLock().unlock();
			}
        }

        private boolean isEmpty() {
			lock.readLock().lock();
			try {
//            return local.isEmpty() && remote.isEmpty();
//			return localConsumers.isEmpty() && remoteConsumers.isEmpty();
			return localConsumers.isEmpty() && remote.isEmpty();
			} finally {
				lock.readLock().unlock();
			}
        }
        
		private class ThreadedConsumer<D> implements WorkConsumer<D> {

			private boolean keepRunnning = true;
			private CircularBlockingDeque<D> queue;
			private Thread thread;
			private volatile long lastMessageTime;

			public ThreadedConsumer(Link<D> subscriber, int queueSize, int timeout) {
				this.queue = new CircularBlockingDeque<>(queueSize);
				Class<? extends D> type = subscriber.getType();
				String typeName = type == null ? "null" : type.getName();
				String name = subscriber.getThreadName();
				this.lastMessageTime = System.currentTimeMillis();
				this.thread = new Thread(subscriber.getThreadName()) {
					@Override
					public void run() {
						if (timeout > 0) {
							while (keepRunnning) {
								try {
									D message = queue.takeFirstWitDeadline(lastMessageTime + timeout);
									if (message == null) {
										queue.clear();
										lastMessageTime = System.currentTimeMillis();
									}
									subscriber.handle(message);
								} catch (InterruptedException e) {
								}
							}
						} else {
							while (keepRunnning) {
								try {
									D message = queue.takeFirst();
									subscriber.handle(message);
								} catch (InterruptedException e) {
								}
							}
						}
					}
				};
				this.thread.start();
			}

			@Override
			public void offer(D message) {
				this.lastMessageTime = System.currentTimeMillis();
				queue.addLast(message);
			}

			@Override
			public void stop() {
				this.keepRunnning = false;
				this.thread.interrupt();
			}
		}

		private class WorkQueueConsumer<D> implements WorkConsumer<D> {
			
			private MessageProcessor processor;

			public WorkQueueConsumer(Link<D> subscriber, int queueSize, int timeout) {
				final String nodeName = NodeNameManager.getNodeName();
				this.processor = factory.createProcessor(new Consumer() {
					@Override
					public void accept(Object message) {
						NodeNameManager.setNodeName(nodeName);
						subscriber.handle((D) message);
					}
				}, queueSize, timeout);
			}

			@Override
			public void offer(D message) {
				this.processor.addMessage(message);
			}

			@Override
			public void stop() {
				this.processor.stop();
			}
		}
    }
	
	public static boolean USE_THREADED_CONSUMER = false;
	public static boolean USE_THREADED_REPEATER = false;
	public static int WORK_QUEUE_MAX_SIZE = 500;
	public static int WORK_QUEUE_BUFFER_SIZE = 20;
	public static int SCHEDULER_POOL_SIZE = 2;

	public static MessageProcessorFactory factory = new MessageProcessorFactory(WORK_QUEUE_MAX_SIZE, WORK_QUEUE_BUFFER_SIZE);

	interface WorkConsumer<D> {
		void offer(D message);
		void stop();
	}

    private final HashMap<Class<?>, Receive<?>> publishers;
    private final HashMap<Class<?>, Deliver<?>> listeners;
    
    public LinkManager() {
        publishers = new HashMap<>();
        listeners = new HashMap<>();
    }

	public <D> Link<D> getPublisher(Class<? extends D> from, boolean isLocal, boolean latched) {
        Receive<?> publisher;
        lock.readLock().lock();
        try {
            publisher = publishers.get(from);
        } finally {
            lock.readLock().unlock();
        }
            
        if (publisher == null) {
            // ensure that converters are pre-cached
            // throw an exception (don't add the publisher) if it cannot be cached
//            for (Class<?> to : listeners.keySet()) {
//                if (isLocal || listeners.get(to).hasLocalClients())
//                    TypeConverter.precompile(from, to);
//            }
// no need to precompile - compiled at run time
// TODO: verify that conversion exist

// moved to create publisher/subscriber methods
//        	// load type converters at runtime
//			if (!from.equals(RosMessage.class)) {
//            	RosTypeConverters.precompile(from);
//			}
//			for (Class<?> to : listeners.keySet()) {
//				if (!from.equals(RosMessage.class)) {
//	            	RosTypeConverters.precompile(to);
//				}
//			}

            lock.writeLock().lock();
            try {
				publishers.put(from, publisher = new Receive<>(from, isLocal, latched));
            } finally {
                lock.writeLock().unlock();
            }
        }
		return (Link<D>) publisher;
    }

	public <D> void subscribe(Link<D> link, boolean isLocal) {
		subscribe(link, isLocal, 1, 0);
	}
    
	public <D> void subscribe(Link<D> link, boolean isLocal, int queueSize, int timeout) {
        ArrayList<D> latched;
        lock.readLock().lock();
        try {
            Class<? extends D> to = link.getType();
            Deliver<D> deliver = (Deliver<D>)listeners.get(to);
            if (deliver == null) {
                // ensure that converters are pre-cached
                // throw an exception (don't add the publisher) if it cannot be cached
//				for (Class<?> from : publishers.keySet()) {
//					// WOJ: fix for pub/sub local and remote at the same time
//					if (!from.equals(RosMessage.class) || !to.equals(RosMessage.class)) {
//						TypeConverter.precompile(from, to);
//					}
//				}
// no need to precompile - compiled at run time
// TODO: verify that conversion exist
            	
// moved to create publisher/subscriber methods
//            	// load type converters at runtime
//				for (Class<?> from : publishers.keySet()) {
//					if (!from.equals(RosMessage.class)) {
//		            	RosTypeConverters.precompile(from);
//					}
//				}
//				if (!to.equals(RosMessage.class)) {
//	            	RosTypeConverters.precompile(to);
//				}

                lock.readLock().unlock();
                lock.writeLock().lock();
                try {
                    listeners.put(to, deliver = new Deliver<>(to));
                } finally {
                    lock.writeLock().unlock();
                    lock.readLock().lock();
                }

            }
			deliver.add(link, isLocal, queueSize, timeout);

            // Pass latched messages to new subscribers
            // Note that we need to convert them again because we don't cache the conversions
            latched = new ArrayList<>();
			for (Receive<?> r : publishers.values()) {
				// woj fix for message being null/empty for latched topics without published message
				if (r.isLatched && r.latchedValue != null) {
					try {
						// WOJ: Handle case where subscriber wishes to accept all types: "*"
						// In that case to == null
						if (to == null) {
							r.computeRemoteTypeWithoutLock();
							to = r.remoteToType;
						}
						
						// TODO: this should be moved to message converter code generator
						if (r.latchedValue instanceof RosMessage) {
							// make sure that when ros message is handled for the second time
							// the buffer will be at start position
							((RosMessage) r.latchedValue).reset();
						}
						TypeConverter converter = RosTypeConvertersSerializationWrapper.get(r.fromType, to);
						if (converter == null) {
							String toTypeStr = "null";
							if (to != null) {
								toTypeStr = to.getCanonicalName();
							}
							LOG.error("Unable to convert message from " + r.fromType.getCanonicalName() + " to "
									+ toTypeStr + ", remote ros type " + r.remoteRosType + ", remote java type  "
									+ r.remoteJavaType);
							continue;
						}
						Object converted = converter.convert(r.latchedValue);
						latched.add((D) converted);
					} catch (Exception e) {
						LOG.error("Exception caught when handling message: " + r.fromType + " " + r.latchedValue, e);
					}
				}
			}
        } finally {
            lock.readLock().unlock();
        }
        
        for (D message : latched)
            link.handle(message);

    }
    
    public <D> void unsubscribe(Link<D> link) {
        lock.writeLock().lock();
        try {
            Class<? extends D> to = link.getType();
            Deliver<D> deliver = (Deliver<D>)listeners.get(to);
            if (deliver != null) {
                deliver.remove(link);
                if (deliver.isEmpty())
                    listeners.remove(to);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public boolean hasSubscribers() {
        lock.readLock().lock();
        try {
            return listeners.size() > 0;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public boolean hasLocalSubscribers() {
        lock.readLock().lock();
        try {
            if (listeners.isEmpty())
                return false;
            for (Deliver<?> deliver : listeners.values())
                if (deliver.hasClients(true))
                    return true;
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

	public int getNumberOfMessageListeners() {
		lock.readLock().lock();
		try {
			int num = 0;
			for(Deliver<?> listener : listeners.values()) {
//				num += listener.remoteConsumers.size();
				num += listener.remote.size();
				num += listener.localConsumers.size();
			}
			return num;
		} finally {
			lock.readLock().unlock();
		}
	}

	public void skipLocalMessages(boolean skip) {
		this.sendLocalMessages = !skip;
	}

	public void clearLatchedValues() {
		lock.writeLock().lock();
		try {
			for (Receive<?> publisher : publishers.values()) {
				publisher.latchedValue = null;
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

}
