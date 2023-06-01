package com.github.jy2.smlib3.utils;

import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.api.LogSeldom;
import com.github.jy2.smlib.messages.SmState;
import com.github.jy2.smlib.messages.SmStructure;
import com.github.jy2.smlib3.StateMachine;

public class StatePublisher {

	public static final LogSeldom LOG = JyroscopeDi.getLog();

	private String name;
	private StateMachine<?, ?> stateMachine;

	private com.github.jy2.Publisher<SmStructure> smStructurePublisher;
	private com.github.jy2.Publisher<SmState> smStatePublisher;

	private int interval;
	private boolean shutdown = false;
	private Thread thread;

	private JyroscopeDi jyDi;

	public StatePublisher(JyroscopeDi jyroscopeDi, StateMachine<?, ?> sm, int interval) {
		this.jyDi = jyroscopeDi;
		this.name = jyroscopeDi.getName().toString();
		this.stateMachine = sm;
		this.interval = interval;
	}

	public static StatePublisher start(JyroscopeDi jyroscopeDi, StateMachine<?, ?> sm, int interval) {
		StatePublisher sp = new StatePublisher(jyroscopeDi, sm, interval);
		sp.start();
		return sp;
	}

	public void start() {
		if (stateMachine == null) {
			return;
		}
		this.smStructurePublisher = jyDi.createPublisher("/" + name + "/sm_structure", SmStructure.class);
		this.smStatePublisher = jyDi.createPublisher("/" + name + "/sm_state", SmState.class);

		this.thread = new Thread(() -> {
			try {
				while (!shutdown) {
					try {
						smStructurePublisher.publish(stateMachine.getSmStructure());
						smStatePublisher.publish(stateMachine.getSmState());
					} catch (Exception t1) {
						LOG.error("Uncaught exception in state publisher", t1);
					}

					try {
						Thread.sleep(interval);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}

				try {
					smStatePublisher.publish(stateMachine.getSmState());
				} catch (Exception t2) {
					LOG.error("Uncaught exception in state publisher", t2);
				}

				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			} catch (Exception e) {
				LOG.error("Uncaught exception in state publisher", e);
			}

		}, "StatePublisherThread");
		thread.start();

	}

	public void stop() {
		shutdown = true;
		thread.interrupt();
	}
}
