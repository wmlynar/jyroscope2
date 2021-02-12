package com.jyroscope.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import com.jyroscope.Link;
import com.jyroscope.Name;

public class LocalTopic<T> implements Topic<T> {
    
    private Set<Class<? extends T>> types;
    private List<Link<T>> subscribers;
	private HashMap<Link<T>, T> latched;
    private Name<LocalTopic> name;
	private HashMap<Link<T>, Consumer<T>> consumers;
	private boolean isLatched;
    
    public LocalTopic(Name<LocalTopic> name) {
        this.name = name;
        this.subscribers = new ArrayList<>();
        this.types = new HashSet<>();
        this.latched = new HashMap<>();
		this.consumers = new HashMap<>();
    }

	@Override
	public synchronized void subscribe(Link<T> subscriber) {
		subscribe(subscriber, 1);
	}

    @Override
	public synchronized void subscribe(Link<T> subscriber, int queueSize) {
        types.add(subscriber.getType());
        subscribers.add(subscriber);

		Consumer<T> consumer = new Consumer<>(subscriber, queueSize);
		consumers.put(subscriber, consumer);
        
        ArrayList<T> copy;
        synchronized (latched) {
            copy = new ArrayList<>(latched.values());
        }
        for (T message : copy) {
//            subscriber.handle(message);
			consumer.offer(message);
        }

		consumer.start();
    }

    @Override
    public synchronized void unsubscribe(Link<T> subscriber) {
        subscribers.remove(subscriber);
		Consumer<T> consumer = consumers.remove(subscriber);
		if (consumer != null) {
			consumer.stop();
		}
    }

    @Override
	public <D> Link<D> getPublisher(Class<? extends D> type, boolean isLatched) {
		this.isLatched = isLatched;
		return new Link<D>() {

            @Override
            public Class<? extends D> getType() {
                return type;
            }

            @Override
            public void handle(D message) {
				if (isLatched) {
					synchronized (latched) {
						latched.put((Link<T>) this, (T) message);
					}
				}
                process(message);
            }
            
            private void process(D message) {
                // TODO do type conversion rather than just a naive cast
//                for (Link<T> link : subscribers)
//                    Do.deliver(message, (Link<D>)link);
				for (Consumer<T> consumer : consumers.values()) {
					consumer.offer((T) message);
				}
            }

			@Override
			public void setRemoteAttributes(boolean isLatched, String remoteRosType, String remoteJavaType) {
				throw new UnsupportedOperationException("Reserved for class Receive in LinkManager");
			}

			@Override
			public String getThreadName() {
				throw new UnsupportedOperationException("No thread name");
			}
        };
    }

	@Override
	public boolean isLatched() {
		return isLatched;
	}

	@Override
	public boolean isRemoteLatched() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRemoteJavaType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRemoteRosType() {
		throw new UnsupportedOperationException();
	}
	
	private static class Consumer<D> {

		private boolean keepRunnning = true;
		private ArrayBlockingQueue<D> queue;
		private Thread thread;

		public Consumer(Link<D> subscriber, int queueSize) {
			this.queue = new ArrayBlockingQueue<>(queueSize);
			this.thread = new Thread("LocalTopic.Consumer-" + subscriber.getType().getName()) {
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

	@Override
	public int getNumberOfMessageListeners() {
		return consumers.size();
	}

	@Override
	public void skipLocalMessages(boolean skip) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Name<LocalTopic> getName() {
		return name;
	}

	@Override
	public void setQueueSize(int queueSize) {
		// do nothing
	}

	@Override
	public int getQueueSize() {
		return 1;
	}

}
