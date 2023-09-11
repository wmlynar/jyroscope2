package com.github.jy2.workqueue;

public class Main2 {

	static long lastTime = System.currentTimeMillis();

	public static void main(String[] args) throws InterruptedException {

		MessageProcessorFactory<String> factory = new MessageProcessorFactory<>(10);
		MessageProcessor<String> processor = factory.createProcessor(t -> extracted(t), 5, 100);

		for (int i = 0; i < 10; i++) {
			processor.addMessage("message " + i);
			Thread.sleep(90);
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
