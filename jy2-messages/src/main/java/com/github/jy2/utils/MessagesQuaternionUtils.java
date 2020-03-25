package com.github.jy2.utils;

import javax.vecmath.Quat4d;

import go.jyroscope.ros.geometry_msgs.Quaternion;

public class MessagesQuaternionUtils {

	public static double quaternionToYawEulerAngle(double qw, double qz) {
		double tempA = 2 * qw * qz;
		double tempB = 1 - 2 * qz * qz;
		return Math.atan2(tempA, tempB);
	}

	public static double quaternionToYawEulerAngle(Quat4d q) {
		double tempA = 2 * q.w * q.z;
		double tempB = 1 - 2 * q.z * q.z;
		return Math.atan2(tempA, tempB);
	}

	public static double quaternionToYawEulerAngle(Quaternion q) {
		double tempA = 2 * q.w * q.z;
		double tempB = 1 - 2 * q.z * q.z;
		return Math.atan2(tempA, tempB);
	}
	
	public static Quat4d yawEulerAngleToQuaternion(double yaw) {
		return yawEulerAngleToQuaternion(yaw, new Quat4d());
	}

	public static Quat4d yawEulerAngleToQuaternion(double yaw, Quat4d quaternion) {

		quaternion.x = 0;
		quaternion.y = 0;
		quaternion.w = Math.cos(yaw * 0.5);
		quaternion.z = Math.sin(yaw * 0.5);

		return quaternion;
	}

	public static Quaternion yawEulerAngleToQuaternion(double yaw, Quaternion quaternion) {

		quaternion.x = 0;
		quaternion.y = 0;
		quaternion.w = Math.cos(yaw * 0.5);
		quaternion.z = Math.sin(yaw * 0.5);

		return quaternion;
	}

	public static Quaternion yawEulerAngleToQuaternion2(double yaw) {
		return yawEulerAngleToQuaternion(yaw, new Quaternion());
	}

}
