package com.github.jy2.commandline.picocli.tf.format;

import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

public class QuaternionUtils {

	public static Quat4d eulerToQuaternion(Vector3d rotation, Quat4d q) {
		double eulerX = rotation.x;
		double eulerY = rotation.y;
		double eulerZ = rotation.z;

		double sx = Math.sin(eulerX / 2);
		double sy = Math.sin(eulerY / 2);
		double sz = Math.sin(eulerZ / 2);
		double cx = Math.cos(eulerX / 2);
		double cy = Math.cos(eulerY / 2);
		double cz = Math.cos(eulerZ / 2);
		double cycz = cy * cz;
		double sysz = sy * sz;
		double d = cycz * cx - sysz * sx;
		double a = cycz * sx + sysz * cx;
		double b = sy * cz * cx + cy * sz * sx;
		double c = cy * sz * cx - sy * cz * sx;

		q.x = a;
		q.y = b;
		q.z = c;
		q.w = d;

		q.normalize();

		return q;
	}

	public static Vector3d quaternionToEuler(Quat4d q, Vector3d v) {
		double heading;
		double attitude;
		double bank;

		double sqw = q.w * q.w;
		double sqx = q.x * q.x;
		double sqy = q.y * q.y;
		double sqz = q.z * q.z;
		double unit = sqx + sqy + sqz + sqw; // if normalised is one, otherwise is correction factor
		double test = q.x * q.y + q.z * q.w;
		heading = Math.atan2(2 * q.y * q.w - 2 * q.x * q.z, sqx - sqy - sqz + sqw);
		if (test > 0.4999999999 * unit) { // singularity at north pole
			// heading = 2 * Math.atan2(q.x, q.w);
			attitude = Math.PI / 2;
			// bank = 0;
		} else if (test < -0.4999999999 * unit) { // singularity at south pole
			// heading = -2 * Math.atan2(q.x, q.w);
			attitude = -Math.PI / 2;
			// bank = 0;
		} else {
			attitude = Math.asin(2 * test / unit);
		}
		bank = Math.atan2(2 * q.x * q.w - 2 * q.y * q.z, -sqx + sqy - sqz + sqw);

		v.x = bank;
		v.y = heading;
		v.z = attitude;

		return v;
	}

	public static Quat4d rpyToQuaternion(Vector3d rotation, Quat4d q) {
		double halfYaw = rotation.z * 0.5f;
		double halfPitch = rotation.y * 0.5f;
		double halfRoll = rotation.x * 0.5f;
		double cosYaw = (double) Math.cos(halfYaw);
		double sinYaw = (double) Math.sin(halfYaw);
		double cosPitch = (double) Math.cos(halfPitch);
		double sinPitch = (double) Math.sin(halfPitch);
		double cosRoll = (double) Math.cos(halfRoll);
		double sinRoll = (double) Math.sin(halfRoll);
		q.x = sinRoll * cosPitch * cosYaw - cosRoll * sinPitch * sinYaw;
		q.y = cosRoll * sinPitch * cosYaw + sinRoll * cosPitch * sinYaw;
		q.z = cosRoll * cosPitch * sinYaw - sinRoll * sinPitch * cosYaw;
		q.w = cosRoll * cosPitch * cosYaw + sinRoll * sinPitch * sinYaw;

		q.normalize();

		return q;
	}

	// https://computergraphics.stackexchange.com/questions/8195/how-to-convert-euler-angles-to-quaternions-and-get-the-same-euler-angles-back-fr
	public static Vector3d quaternionToRpy(Quat4d q, Vector3d v) {
		double heading;
		double attitude;
		double bank;

		double sqw = q.w * q.w;
		double sqx = q.x * q.x;
		double sqy = q.y * q.y;
		double sqz = q.z * q.z;
		double unit = sqx + sqy + sqz + sqw; // if normalised is one, otherwise is correction factor
		double test = q.w * q.y - q.z * q.x;

		// FIXME: fix when singularity for heading and bank...
		heading = Math.atan2(2 * q.w * q.x + 2 * q.y * q.z, 1.0 - 2 * sqx - 2 * sqy);
		if (test > 0.4999999999 * unit) { // singularity at north pole
			// heading = 0;
			attitude = Math.PI / 2;
			// bank = 2 * Math.atan2(q.z, q.w);
		} else if (test < -0.4999999999 * unit) { // singularity at south pole
			// heading = 0;
			attitude = -Math.PI / 2;
			// bank = 2 * Math.atan2(q.z, q.w);
		} else {
			attitude = Math.asin(2 * test / unit);
		}
		bank = Math.atan2(2 * q.w * q.z + 2 * q.x * q.y, 1.0 - 2 * sqy - 2 * sqz);

		v.x = heading;
		v.y = attitude;
		v.z = bank;

		return v;
	}

	public static Quat4d eulerToQuaternion2(Vector3d rotation, Quat4d q) {
		double halfYaw = rotation.z * 0.5f;
		double halfPitch = rotation.y * 0.5f;
		double halfRoll = rotation.x * 0.5f;
		double cosYaw = (double) Math.cos(halfYaw);
		double sinYaw = (double) Math.sin(halfYaw);
		double cosPitch = (double) Math.cos(halfPitch);
		double sinPitch = (double) Math.sin(halfPitch);
		double cosRoll = (double) Math.cos(halfRoll);
		double sinRoll = (double) Math.sin(halfRoll);

		// q.x = sinRoll * cosPitch * cosYaw - cosRoll * sinPitch * sinYaw;
		q.x = sinRoll * cosPitch * cosYaw + cosRoll * sinPitch * sinYaw;
		q.y = cosRoll * sinPitch * cosYaw + sinRoll * cosPitch * sinYaw;
		q.z = cosRoll * cosPitch * sinYaw - sinRoll * sinPitch * cosYaw;
		// q.w = cosRoll * cosPitch * cosYaw + sinRoll * sinPitch * sinYaw;
		q.w = cosRoll * cosPitch * cosYaw - sinRoll * sinPitch * sinYaw;

		q.normalize();

		return q;
	}

}
