package com.inovatica.autofork.orchestrator.ui;

import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.di.annotations.Init;
import com.github.jy2.di.annotations.Inject;
import com.github.jy2.di.annotations.Parameter;
import com.github.jy2.di.annotations.Repeat;
import com.inovatica.orchestrator.json.OrcherstratorStatus;

import go.jyroscope.ros.diagnostic_msgs.KeyValue;
import go.jyroscope.ros.rosgraph_msgs.Log;

public class DistributedInterface {

    public static final org.apache.commons.logging.Log LOG = JyroscopeDi.getLog();
    private static final int STATUS_MESSAGES_QUEUE_LENGTH = 1;
    private static final int LOG_MESSAGES_QUEUE_LENGTH = 10;
    public ApplicationFrame applicationFrame;

    @Parameter("connect_topic_on_startup")
    public String connectTopicOnStartup = "";
    public com.github.jy2.Publisher<KeyValue> commandPublisher;
    public com.github.jy2.Subscriber<OrcherstratorStatus> statusSubscriber;
    public com.github.jy2.Subscriber<Log> logSubscriber;
    @Inject
    JyroscopeDi di;
    Object statusSubscriberId = "";
    Object logSubscriberId = "";

    @Init
    public void init() {
        if (connectTopicOnStartup == null) {
            connectTopicOnStartup = "";
        }
        connectTopicOnStartup = connectTopicOnStartup.trim();
        applicationFrame.onStartup(connectTopicOnStartup);
    }

    @Repeat(interval = 1000)
    public void repeat() {
        applicationFrame.updateConsole();
    }

    public void onStatusMessage(OrcherstratorStatus status) {
        applicationFrame.setStatus(status);
    }

    public void onStartItem(String itemName) {
        commandPublisher.publish(new KeyValue("start", itemName));
    }

    public void onStopItem(String itemName) {
        commandPublisher.publish(new KeyValue("stop", itemName));
    }

    public void onKillItem(String itemName) {
        commandPublisher.publish(new KeyValue("kill", itemName));
    }

    public void onScanItems() {
        commandPublisher.publish(new KeyValue("scan", ""));
    }

    public void onConnect(String address) {
        LOG.info("Connect button pressed: " + address);

        statusSubscriber = di.createSubscriber(address + "/status", OrcherstratorStatus.class);
        statusSubscriberId = statusSubscriber.addMessageListener(this::onStatusMessage, STATUS_MESSAGES_QUEUE_LENGTH);

        logSubscriber = di.createSubscriber(address + "/output", Log.class);
        logSubscriberId = logSubscriber.addMessageListener(this::receiveLog, LOG_MESSAGES_QUEUE_LENGTH);
        commandPublisher = di.createPublisher(address + "/command", KeyValue.class);
    }

    public void onDisconnect() {
        LOG.info("Disconnect button pressed");
        //statusSubscriber.removeAllMessageListeners();
        di.deleteSubscriber(statusSubscriber);
        //logSubscriber.removeAllMessageListeners();
        di.deleteSubscriber(logSubscriber);

        // somehow adding this causes weird behavior on reconnect (inability to send
        // messages, or receiving multiple messages)
        // statusSubscriber.shutdown();
        // logSubscriber.shutdown();
        // commandPublisher.shutdown();
    }

    public void receiveLog(Log message) {
		if (applicationFrame.isFilteringEnabled()) {
			if (!message.name.equals(applicationFrame.getSelectedItemName())) {
				return;
			}
		}
        String text = "<span style=\"color:blue\">" + message.name + "</span> " + levelToString(message.level)
                + " : " + message.msg;
        applicationFrame.addText(text);
    }

    private String levelToString(byte level) {
        switch (level) {
            case Log.DEBUG:
                return "<span style=\"color:green\">DBG</span>";
            case Log.INFO:
                return "<span style=\"color:green\">INF</span>";
            case Log.WARN:
                return "<span style=\"color:red\">WARN</span>";
            case Log.ERROR:
                return "<span style=\"color:red\">ERR</span>";
            case Log.FATAL:
                return "<span style=\"color:red\">FATAL</span>";
        }
        return "UNKNOWN";
    }

}
