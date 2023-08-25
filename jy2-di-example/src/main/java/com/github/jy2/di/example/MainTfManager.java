package com.github.jy2.di.example;

import java.io.IOException;

import javax.vecmath.Matrix4d;

import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.di.exceptions.CreationException;
import com.github.jy2.tf.mat.TfManager;

import go.jyroscope.ros.geometry_msgs.TransformStamped;

public class MainTfManager {

	public static void main(String[] args) throws CreationException, IOException {

		JyroscopeDi hzDi = new JyroscopeDi("jy2_di_example", args);
		TfManager tfManager = hzDi.create(TfManager.class);
		hzDi.start();
		
		Matrix4d mat = new Matrix4d();
		mat.setIdentity();
		tfManager.add(new TransformStamped(10, "aaa", "bbb", mat));
		tfManager.getTransform("aaa", "bbb", 0, mat);
	}

}
