package com.github.jy2.commandline.picocli.tf.completion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

import com.github.jy2.Subscriber;
import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.tf.mat.TfManager;

import go.jyroscope.ros.geometry_msgs.TransformStamped;
import go.jyroscope.ros.tf2_msgs.TFMessage;
import picocli.AutoComplete;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;

public class TfParentOptionCompletionCandidates implements Iterable<String> {

	@Override
	public Iterator<String> iterator() {
		ArrayList<String> list = new ArrayList<>();
		if (AutoComplete.argIndex != getTfParentIndex() + 1) {
			return list.iterator();
		}
		Subscriber<TFMessage> subscriber = null;
		try {
			subscriber = Main.di.createSubscriber("/tf", TFMessage.class);
			TfManager tfManager = new TfManager();
			subscriber.addMessageListener(new Consumer<TFMessage>() {
				@Override
				public void accept(TFMessage t) {
					try {
						tfManager.add(t);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			});
			try {
				// static transforms usually published every 1000ms, so wait 500ms more
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			subscriber.removeAllMessageListeners();
			ArrayList<TransformStamped> tfList = tfManager.getTransformList();
			for (TransformStamped t : tfList) {
				list.add(t.header.frameId);
			}
		} finally {
			if (subscriber != null) {
				subscriber.removeAllMessageListeners();
			}
		}
		return list.iterator();
	}

	private int getTfParentIndex() {
		if (AutoComplete.tentativeMatch == null) {
			return -1;
		}
		int i = 0;
		for (Object obj : AutoComplete.tentativeMatch) {
			if (obj instanceof CommandSpec) { // subcommand
			} else if (obj instanceof OptionSpec) { // option
				OptionSpec opt = (OptionSpec) obj;
				if ("--parent".equals(opt.longestName())) {
					return i;
				}
			} else if (obj instanceof PositionalParamSpec) { // positional
			}
			i++;
		}
		return -1;
	}
}
