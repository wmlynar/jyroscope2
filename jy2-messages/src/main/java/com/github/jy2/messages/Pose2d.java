package com.github.jy2.messages;

import java.io.Serializable;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Tuple2d;
import javax.vecmath.Tuple3d;

import com.github.jy2.utils.MessagesQuaternionUtils;

import go.jyroscope.ros.geometry_msgs.PoseStamped;
import go.jyroscope.ros.geometry_msgs.PoseWithCovarianceStamped;

public class Pose2d implements Serializable {

	private static final long serialVersionUID = 6429829563484164098L;

	public double x;
	public double y;
	public double a;

	public Pose2d() {
	}

	public Pose2d(PoseStamped pose) {
		set(pose);
	}

	public Pose2d(PoseWithCovarianceStamped pose) {
		set(pose);
	}

	public Pose2d(double x, double y, double a) {
		this.x = x;
		this.y = y;
		this.a = a;
	}

	public Pose2d(Tuple2d p, double angle) {
		set(p, angle);
	}

	public Pose2d(Matrix4d mat) {
		set(mat);
	}

	public Pose2d(Pose2d p) {
		set(p);
	}

	public void set(Pose2d p) {
		this.x = p.x;
		this.y = p.y;
		this.a = p.a;
	}

	public Pose2d set(double x, double y, double a) {
		this.x = x;
		this.y = y;
		this.a = a;
		return this;
	}

	public void set(Tuple2d p, double angle) {
		this.x = p.x;
		this.y = p.y;
		this.a = angle;
	}

	public Pose2d set(Matrix4d t) {
		this.x = t.m03;
		this.y = t.m13;
		this.a = -Math.atan2(t.m01, t.m00);
		return this;
	}

	public Pose2d set(Point3d p) {
		this.x = p.x;
		this.y = p.y;
		return this;
	}

	public Pose2d set(Quat4d q) {
		a = MessagesQuaternionUtils.quaternionToYawEulerAngle(q);
		return this;
	}

	public void get(Matrix4d matrix) {
		matrix.rotZ(a);
		matrix.m03 = x;
		matrix.m13 = y;
	}

	public void get(Tuple3d translation) {
		translation.set(x, y, 0);
	}

	public void get(Quat4d rotation) {
		MessagesQuaternionUtils.yawEulerAngleToQuaternion(a, rotation);
	}

	public void set(PoseStamped pose) {
		this.x = pose.pose.position.x;
		this.y = pose.pose.position.y;
		this.a = MessagesQuaternionUtils.quaternionToYawEulerAngle(pose.pose.orientation);
	}

	public void set(PoseWithCovarianceStamped pose) {
		this.x = pose.pose.pose.position.x;
		this.y = pose.pose.pose.position.y;
		this.a = MessagesQuaternionUtils.quaternionToYawEulerAngle(pose.pose.pose.orientation);
	}

	public void add(Pose2d pose) {
		x += pose.x;
		y += pose.y;
		a += pose.a;
	}

	public void sub(Pose2d pose) {
		x -= pose.x;
		y -= pose.y;
		a -= pose.a;
	}

	public void negate() {
		x = -x;
		y = -y;
		a = -a;
	}

	public void transformBy(Matrix4d matrix) {
		Matrix4d m = new Matrix4d();
		Matrix4d m2 = new Matrix4d(matrix);
		get(m);
		m2.mul(m);
		set(m2);
	}

	@Override
	public String toString() {
		return "Pose2d{" + "x=" + x + ", y=" + y + ", a=" + a + '}';
	}

}
