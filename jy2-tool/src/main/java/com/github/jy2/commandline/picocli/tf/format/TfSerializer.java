package com.github.jy2.commandline.picocli.tf.format;

import javax.vecmath.Vector3d;

import go.jyroscope.ros.geometry_msgs.TransformStamped;

public class TfSerializer {

	public static String serialize(TransformStamped t, TfDisplayFormat format) {
		if (format.equals(TfDisplayFormat.mat)) {
			return "time: " + t.header.toSeconds() + " " + typeToString(t) + t.header.frameId + "->" + t.childFrameId + "\n"
					+ t.asMatrix().toString();
		} else if (format.equals(TfDisplayFormat.quat)) {
			return String.format(
					"time: %.3f %s%s->%s\n\t\ttranslation: [%.3f,%.3f,%.3f] quaternion: [%.3f,%.3f,%.3f,%.3f]", t.header.toSeconds(),
					typeToString(t), t.header.frameId, t.childFrameId, t.transform.translation.x, t.transform.translation.y, t.transform.translation.z,
					t.transform.rotation.x, t.transform.rotation.y, t.transform.rotation.z, t.transform.rotation.w);
//			return "time: " + t.time + " parent: " + t.parentFrameId + " child: " + t.childFrameId
//					+ "\n\t\ttranslation: [ " + t.translation.x + "," + t.translation.y + "," + t.translation.z
//					+ "] quaternion: [" + t.rotation.x + "," + t.rotation.y + "," + t.rotation.z + "," + t.rotation.w
//					+ "]";
		} else {
			Vector3d v = QuaternionUtils.quaternionToRpy(t.transform.rotation.asQuat4d(), new Vector3d());
			return String.format("time: %.3f %s%s->%s\n\t\ttranslation: [%.3f,%.3f,%.3f] rotation: [%.3f,%.3f,%.3f]",
					t.header.toSeconds(), typeToString(t), t.header.frameId, t.childFrameId, t.transform.translation.x, t.transform.translation.y,
					t.transform.translation.z, v.z, v.y, v.x);
//			return "time: " + t.time + " parent: " + t.parentFrameId + " child: " + t.childFrameId
//					+ "\n\t\ttranslation: [ " + t.translation.x + "," + t.translation.y + "," + t.translation.z
//					+ "] rotation: [" + v.z + "," + v.y
//					+ "," + v.x + "]";
		}
	}

	private static String typeToString(TransformStamped t) {
		if (t.childFrameId.contains("static")) {
			return "static ";
		} else if (t.childFrameId.contains("semi")) {
			return "semi ";
		}
		return "";
//		switch(t.type) {
//		case Transform.STATIC:
//			return "static ";
//		case Transform.SEMI:
//			return "semi ";
//		default:
//			return "";
//		}
	}

}
