package com.github.jy2.tf.mat;

import javax.vecmath.Matrix4d;

import com.github.jy2.messages.Pose2d;

import go.jyroscope.ros.geometry_msgs.PoseWithCovarianceStamped;

public class TransformMatrix {

	public double time;
	public String parentFrameId;
	public String childFrameId;

	public Matrix4d matrix = new Matrix4d();

	public void set(TransformMatrix t) {
		time = t.time;
		parentFrameId = t.parentFrameId;
		childFrameId = t.childFrameId;
		matrix.set(t.matrix);
	}

	public void set(String parentFrameId, String childFrameId, double time, Pose2d pose) {
		this.time = time;
		this.parentFrameId = parentFrameId;
		this.childFrameId = childFrameId;
		pose.get(matrix);
	}
	
	public void set(String parentFrameId, String childFrameId, double time, PoseWithCovarianceStamped pose) {
		this.time = time;
		this.parentFrameId = parentFrameId;
		this.childFrameId = childFrameId;
		pose.get(matrix);
	}

}
