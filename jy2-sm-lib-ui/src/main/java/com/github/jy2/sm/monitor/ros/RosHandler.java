package com.github.jy2.sm.monitor.ros;

import java.text.DecimalFormat;

import org.apache.commons.logging.Log;

import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.di.annotations.Inject;
import com.github.jy2.sm.monitor.graph.Graph;
import com.github.jy2.sm.monitor.ui.ApplicationFrame;
import com.github.jy2.smlib.messages.SmState;
import com.github.jy2.smlib.messages.SmStructure;
import com.github.jy2.smlib.messages.StateTransition;

public class RosHandler {

	public static final Log LOG = JyroscopeDi.getLog();

	@Inject
	JyroscopeDi connectedNode;

	public ApplicationFrame applicationFrame;

	// private BufferedImage image;

	private com.github.jy2.Subscriber<SmStructure> structureSubscriber;
	private com.github.jy2.Subscriber<SmState> stateSubscriber;

	private Graph stateGraph = null;
	private SmStructure lastStructure = null;

	DecimalFormat decimalFormat = new DecimalFormat("0.00");

	public void onConnect(String adress) {
		LOG.info("Connect button pressed: " + adress);

		structureSubscriber = connectedNode.createSubscriber(adress + "/sm_structure", SmStructure.class);
		structureSubscriber.addMessageListener(this::onStructureMessage, 1, 0, 0);

		stateSubscriber = connectedNode.createSubscriber(adress + "/sm_state", SmState.class);
		stateSubscriber.addMessageListener(this::onStateMessage, 1, 0, 0);
	}

	public void onDisconnect() {
		LOG.info("Disconnect button pressed");
		applicationFrame.setImage(null);
		applicationFrame.setTopText("");
		applicationFrame.setMiddleText("");
		applicationFrame.setBottomText("");
		applicationFrame.setTimeText("");
		applicationFrame.setLifeText("");
		applicationFrame.setDurationText("");
		// structureSubscriber.removeAllMessageListeners();
		structureSubscriber.shutdown();
		// stateSubscriber.removeAllMessageListeners();
		stateSubscriber.shutdown();
	}

	protected void onStateMessage(SmState smState) {
		applicationFrame.setTopText(smState.getInput());
		applicationFrame.setMiddleText(smState.getAttributes());
		applicationFrame.setBottomText(smState.getOutput());
		applicationFrame.setTimeText(decimalFormat.format(smState.getTime()));
		applicationFrame.setLifeText(decimalFormat.format(smState.getLife()));
		applicationFrame.setDurationText(decimalFormat.format(smState.getDuration()));
		if (stateGraph != null) {
			applicationFrame.setImage(stateGraph.renderImage(smState.getState()));
		}
	}

	protected void onStructureMessage(SmStructure smStructre) {
		if (smStructre.equals(lastStructure)) {
			return;
		}
		lastStructure = smStructre;
		stateGraph = new Graph();
		stateGraph.setStartNode(smStructre.getStart());
		for (StateTransition transition : smStructre.getTransitions()) {
			stateGraph.addNode(transition.getFrom(), transition.getTo());
		}
		applicationFrame.setImage(stateGraph.renderImage(null));
	}

}
