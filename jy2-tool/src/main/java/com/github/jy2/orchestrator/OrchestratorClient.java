package com.github.jy2.orchestrator;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.github.jy2.Publisher;
import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.orchestrator.utils.Holder;
import com.inovatica.orchestrator.json.OrcherstratorStatus;
import com.inovatica.orchestrator.json.OrchestratorStatusItem;

import go.jyroscope.ros.diagnostic_msgs.KeyValue;

public class OrchestratorClient {

	public static ArrayList<OrchestratorStatusItem> getItemStatuses(String address) {

		String topic = address + "/status";

		OrcherstratorStatus status;
		Holder<OrcherstratorStatus> holder = new Holder<OrcherstratorStatus>(null);
		CountDownLatch latch = new CountDownLatch(1);
		com.github.jy2.Subscriber<OrcherstratorStatus> statusSubscriber = Main.di.createSubscriber(topic,
				OrcherstratorStatus.class);
		Object listenerId = statusSubscriber.addMessageListener(s -> {
			holder.value = s;
			latch.countDown();
		}, 1);
		try {
			latch.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return new ArrayList<OrchestratorStatusItem>();
		} finally {
			statusSubscriber.shutdown();
		}
		status = holder.value;

		return status.items;
	}

	public static ArrayList<String> getItemList(String address) {
		ArrayList<OrchestratorStatusItem> list = getItemStatuses(address);
		ArrayList<String> list2 = new ArrayList<String>();
		for (OrchestratorStatusItem i : list) {
			list2.add(i.name);
		}
		return list2;
	}

	public static void startItem(String address, String itemName) {
		Publisher<KeyValue> commandPublisher = Main.di.createPublisher(address + "/command", KeyValue.class);
		// give time for the listeners to connect
		sleep(500);
		commandPublisher.publish(new KeyValue("start", itemName));
	}

	public static void stopItem(String address, String itemName) {
		Publisher<KeyValue> commandPublisher = Main.di.createPublisher(address + "/command", KeyValue.class);
		// give time for the listeners to connect
		sleep(500);
		commandPublisher.publish(new KeyValue("stop", itemName));
	}

	public static void killItem(String address, String itemName) {
		Publisher<KeyValue> commandPublisher = Main.di.createPublisher(address + "/command", KeyValue.class);
		// give time for the listeners to connect
		sleep(500);
		commandPublisher.publish(new KeyValue("kill", itemName));
	}

	public static void scan(String address) {
		Publisher<KeyValue> commandPublisher = Main.di.createPublisher(address + "/command", KeyValue.class);
		// give time for the listeners to connect
		sleep(500);
		commandPublisher.publish(new KeyValue("scan", ""));
	}

	private static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
		}
	}

}
