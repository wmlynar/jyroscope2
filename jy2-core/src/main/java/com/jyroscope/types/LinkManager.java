package com.jyroscope.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.github.jy2.mapper.RosTypeConverters;
import com.github.jy2.serialization.RosTypeConvertersSerializationWrapper;
import com.jyroscope.Link;
import com.jyroscope.ros.RosMessage;

// TODO handle concurrent access
public class LinkManager {
    
    private ReadWriteLock lock = new ReentrantReadWriteLock();

	private boolean sendLocalMessages = true;
    
    private class Receive<D> implements Link<D> {
        
        private final Class<? extends D> fromType;
        private final boolean isLocal;
        private volatile boolean isLatched;
        private volatile D latchedValue;
        
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
//							TypeConverter converter = TypeConverter.get(fromType, entry.getKey());
							TypeConverter converter;
							if (entry.getKey() == null) {
								// accept all types
								converter = RosTypeConverters.IDENTITY_TYPE_CONVERTER;
								// TODO: why not this?
								// converter = RosTypeConverters.fromRosClass(fromType);
							} else {
								converter = RosTypeConvertersSerializationWrapper.get(fromType, entry.getKey());
							}
							Object converted = converter.convert(message);
							((Deliver) deliver).handle(converted, isLocal);
						} catch (Throwable e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    }
                }
            } finally {
                lock.readLock().unlock();
            }
        }

		@Override
		public void setLatched(boolean isLatched) {
			Receive.this.isLatched = isLatched;
		}
    }
    
	private class Deliver<D> {
        
//        private final Class<? extends D> to;
//        private final ArrayList<Link<D>> local;
        private final ArrayList<Link<D>> remote;
		private HashMap<Link<D>, Consumer<D>> localConsumers;
//		private HashMap<Link<D>, Consumer<D>> remoteConsumers;
        
        private Deliver(Class<? extends D> to) {
//            this.to = to;
//            this.local = new ArrayList<>();
            this.remote = new ArrayList<>();
			this.localConsumers = new HashMap<>();
//			this.remoteConsumers = new HashMap<>();
        }
        
		private void add(Link<D> link, boolean isLocal, int queueSize) {
            if (isLocal) {
    			Consumer<D> consumer = new Consumer<>(link, queueSize);
//                local.add(link);
				localConsumers.put(link, consumer);
				consumer.start();
            }
            else
                remote.add(link);
//				remoteConsumers.put(link, consumer);
        }
        
        private void remove(Link<D> link) {
//            local.remove(link);
            remote.remove(link);
			Consumer<D> consumer = localConsumers.remove(link);
			if (consumer != null) {
				consumer.stop();
			}
//			consumer = remoteConsumers.remove(link);
//			if (consumer != null) {
//				consumer.stop();
//			}
        }
        
        private boolean hasLocalClients() {
//            return local.size() > 0;
			return localConsumers.size() > 0;
        }
        
        private boolean hasClients(boolean forMessageFromLocal) {
//            return local.size() > 0 || (forMessageFromLocal && remote.size() > 0);
//			return localConsumers.size() > 0 || (forMessageFromLocal && remoteConsumers.size() > 0);
			return localConsumers.size() > 0 || (forMessageFromLocal && remote.size() > 0);
        }
        
        private void handle(D message, boolean isLocal) {
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
				for (Consumer<D> consumer : localConsumers.values())
					consumer.offer(message);
//			if (isLocal && remoteConsumers.size() > 0)
//				for (Consumer<D> consumer : remoteConsumers.values())
//					consumer.offer(message);
			if (isLocal && remote.size() > 0)
				for (Link<D> link : remote)
					link.handle(message);
        }

        private boolean isEmpty() {
//            return local.isEmpty() && remote.isEmpty();
//			return localConsumers.isEmpty() && remoteConsumers.isEmpty();
			return localConsumers.isEmpty() && remote.isEmpty();
        }

		private class Consumer<D> {

			private boolean keepRunnning = true;
			private ArrayBlockingQueue<D> queue;
			private Thread thread;

			public Consumer(Link<D> subscriber, int queueSize) {
				this.queue = new ArrayBlockingQueue<>(queueSize);
				Class<? extends D> type = subscriber.getType();
				String typeName = type == null ? "null" : type.getName();
				this.thread = new Thread("LinkManager.Consumer-" + typeName) {
					@Override
					public void run() {
						while (keepRunnning) {
							try {
								D message = queue.take();
								subscriber.handle(message);
							} catch (InterruptedException e) {
							}
						}
					}
				};
			}

			public void offer(D message) {
				queue.offer(message);
			}

			public void start() {
				if (!keepRunnning) {
					throw new IllegalStateException("Consumer was already stopped");
				}
				this.thread.start();
			}

			public void stop() {
				this.keepRunnning = false;
				this.thread.interrupt();
			}
		}
    }
    
    private final HashMap<Class<?>, Receive<?>> publishers;
    private final HashMap<Class<?>, Deliver<?>> listeners;
    
    public LinkManager() {
        publishers = new HashMap<>();
        listeners = new HashMap<>();
    }

	public synchronized <D> Link<D> getPublisher(Class<? extends D> from, boolean isLocal, boolean latched)
			throws ConversionException {
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

	public <D> void subscribe(Link<D> link, boolean isLocal) throws ConversionException {
		subscribe(link, isLocal, 1);
	}
    
	public <D> void subscribe(Link<D> link, boolean isLocal, int queueSize) throws ConversionException {
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
			deliver.add(link, isLocal, queueSize);

            // Pass latched messages to new subscribers
            // Note that we need to convert them again because we don't cache the conversions
            latched = new ArrayList<>();
            for (Receive<?> r : publishers.values())
                synchronized (r) {
                	// woj fix for message being null/empty for latched topics without published message
                    if (r.isLatched && r.latchedValue!=null) {
						try {
//	                        TypeConverter converter = TypeConverter.get(r.fromType, to);
							if(r.fromType.equals(RosMessage.class)) {
								// make sure that when ros message is handled for the second time
								// the buffer will be at start position
								((RosMessage)r.latchedValue).reset();
							}
							TypeConverter converter;
							if (to == null) {
								// accept all types
								converter = RosTypeConverters.IDENTITY_TYPE_CONVERTER;
								// TODO: why not this?
								// converter = RosTypeConverters.fromRosClass(r.fromType);
							} else if (r.fromType.equals(RosMessage.class) && to.equals(RosMessage.class)) {
								converter = RosTypeConverters.IDENTITY_TYPE_CONVERTER;
							} else {
								converter = RosTypeConvertersSerializationWrapper.get(r.fromType, to);
							}
							Object converted = converter.convert(r.latchedValue);
							latched.add((D) converted);
						} catch (Exception e) {
							e.printStackTrace();
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

}
