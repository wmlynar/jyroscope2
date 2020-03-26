package com.github.jy2.tf.mat;

import javax.vecmath.Matrix4d;

import com.github.jy2.messages.Pose2d;

public class TfUtils {

	/**
	 * In the chain map-odom-base it computes map-odom part given the position and
	 * base-odom transform.
	 * 
	 * Derivation below:<br>
	 * 
	 * pose(map) = map-base<br>
	 * pose(map) = map-odom x odom-base<br>
	 * pose(map) x base-odom = map-odom<br>
	 * map-odom = pose(map) x base-odom
	 * 
	 * @param pose2d     Pose of the object
	 * @param baseToOdom Base to odom odometry transform of the object
	 * @param mapToOdom  Matrix where the output map to odom transform will be
	 *                   stored, given base to odom and object position
	 */
	public static void mapToOdomFromPose(Pose2d pose2d, Matrix4d baseToOdom, Matrix4d mapToOdom) {
		pose2d.get(mapToOdom);
		mapToOdom.mul(baseToOdom);

	}

}
