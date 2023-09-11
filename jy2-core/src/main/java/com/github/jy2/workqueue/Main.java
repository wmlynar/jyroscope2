package com.github.jy2.workqueue;

public class Main {

	static long lastTime = System.currentTimeMillis();

	public static void main(String[] args) throws InterruptedException {

		MessageProcessorFactory<String> factory = new MessageProcessorFactory<>(10);
		MessageProcessor<String> processor = factory.createProcessor(t -> extracted(t), 5, 100);

		for (int i = 0; i < 100; i++) {
			factory.createProcessor(t -> {
				System.out.print("a");
			}, 5, 100);
		}

		for (int i = 0; i < 10; i++) {
			processor.addMessage("message " + i);
			Thread.sleep(90);
		}

		Thread.sleep(3000);

		for (int i = 0; i < 100; i++) {
			processor.addMessage("message " + i);
		}
	}

	private static void extracted(String t) {
		long time = System.currentTimeMillis();
		System.out.println("" + (time - lastTime) + " " + t);
		lastTime = time;
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
