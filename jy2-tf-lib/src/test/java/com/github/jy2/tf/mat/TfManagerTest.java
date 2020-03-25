package com.github.jy2.tf.mat;

public class TfManagerTest {

	// @Test
	// public void testTransform() {
	//
	// Pose2d pos1 = new Pose2d();
	// Matrix4d mat1 = new Matrix4d();
	// pos1.x = 1;
	// pos1.updateMatrix(mat1);
	//
	// Pose2d pos2 = new Pose2d();
	// Matrix4d mat2 = new Matrix4d();
	// pos2.y = 1;
	// pos2.updateMatrix(mat2);
	//
	// Pose2d pos3 = new Pose2d();
	// Matrix4d mat3 = new Matrix4d();
	//
	// TfManager tfManager = new TfManager();
	// tfManager.addTransform("a", "b", 0.0, mat1, false);
	// tfManager.addTransform("b", "c", 0.0, mat2, false);
	//
	// boolean exists = tfManager.getTransform("a", "c", 0.0, mat3);
	// assertTrue(exists);
	//
	// pos3.fromTransform(mat3);
	// assertEquals(1, pos3.x, 1e-6);
	// assertEquals(1, pos3.y, 1e-6);
	//
	// exists = tfManager.getTransformLatestIndividually("a", "c", mat3);
	// assertTrue(exists);
	//
	// pos3.fromTransform(mat3);
	// assertEquals(1, pos3.x, 1e-6);
	// assertEquals(1, pos3.y, 1e-6);
	//
	// // in opposite direction
	//
	// exists = tfManager.getTransform("c", "a", 0.0, mat3);
	// assertTrue(exists);
	//
	// pos3.fromTransform(mat3);
	// assertEquals(-1, pos3.x, 1e-6);
	// assertEquals(-1, pos3.y, 1e-6);
	//
	// exists = tfManager.getTransformLatestIndividually("c", "a", mat3);
	//
	// assertTrue(exists);
	//
	// pos3.fromTransform(mat3);
	// assertEquals(-1, pos3.x, 1e-6);
	// assertEquals(-1, pos3.y, 1e-6);
	//
	// }
	//
	// @Test
	// public void testTransformLatest() {
	// Pose2d pos1 = new Pose2d();
	// Matrix4d mat1 = new Matrix4d();
	// pos1.x = 1;
	// pos1.updateMatrix(mat1);
	//
	// Pose2d pos2 = new Pose2d();
	// Matrix4d mat2 = new Matrix4d();
	// pos2.y = 1;
	// pos2.updateMatrix(mat2);
	//
	// Pose2d pos3 = new Pose2d();
	// Matrix4d mat3 = new Matrix4d();
	//
	// TfManager tfManager = new TfManager();
	// tfManager.addTransform("a", "b", 0.0, mat1, false);
	// tfManager.addTransform("a", "b", 1.0, mat1, false);
	// tfManager.addTransform("b", "c", 0.0, mat1, false);
	// tfManager.addTransform("b", "c", 1.0, mat1, false);
	// tfManager.addTransform("b", "c", 2.0, mat2, false);
	// tfManager.addTransform("c", "d", 3.0, mat2, true);
	//
	// boolean exists = tfManager.getTransformLatestSameTime("a", "d", mat3);
	// assertTrue(exists);
	//
	// pos3.fromTransform(mat3);
	//
	// assertEquals(2, pos3.x, 1e-6);
	// assertEquals(1, pos3.y, 1e-6);
	// }

}
