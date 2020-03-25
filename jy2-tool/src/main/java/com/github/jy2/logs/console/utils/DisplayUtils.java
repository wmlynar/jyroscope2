package com.github.jy2.logs.console.utils;

import com.github.jy2.logs.console.model.Level;
import com.github.jy2.logs.console.model.Model;
import com.github.jy2.logs.console.model.Node;
import com.github.jy2.logs.console.model.Place;

import go.jyroscope.ros.rosgraph_msgs.Log;

public class DisplayUtils {

	public static void display(Model model, boolean fullMessage, int maxMessages) {
		int numNodes = model.getNumNodes();
		for (int i = 0; i < numNodes; i++) {
			Node node = model.getNode(i);
			System.out.println(node.name);
			int numLevels = node.getNumLevels();
			for (int j = 0; j < numLevels; j++) {
//				int aaaa = 0;
				Level level = node.getLevel(j);
				int numPlaces = level.getNumPlaces();
				for (int k = 0; k < numPlaces; k++) {
//					int bbbb = 0;
					Place place = level.getPlace(k);
					int numEntries = place.getNumEntries();
					if (numEntries > maxMessages) {
						numEntries = maxMessages;
					}
					for (int l = 0; l < numEntries; l++) {
						Log entry = place.getEntry(l);
						String str;
						String text;
						if (fullMessage) {
							str = entry.msg;
							text = "    " + LogStringUtils.pad(level.toString(), 4) + " " + place.toString() + " "
									+ entry.function + " " + str;
						} else {
							str = LogStringUtils.trim(entry.msg, 100);
							text = "    " + LogStringUtils.pad(level.toString(), 4) + " " + str;
						}
//						String strLevel;
//						if (aaaa == 0) {
//							strLevel = level.toString();
//						} else {
//							strLevel = "";
//						}
//						String strPlace;
//						if (bbbb == 0) {
//							strPlace = place.toString();
//						} else {
//							strPlace = "";
//						}
//						String text = "    " + LogStringUtils.pad(strLevel, 4) + " "
//								+ LogStringUtils.pad(strPlace, 16) + " "
//								+ LogStringUtils.pad(entry.function, 10) + " " + str;
//						System.out.println(text);
//						aaaa++;
//						bbbb++;
						System.out.println(text);
					}
				}
			}
		}

	}

}
