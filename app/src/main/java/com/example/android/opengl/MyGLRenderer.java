/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.opengl;

import javax.microedition.khronos.egl.EGLConfig;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

/**
 * Provides drawing instructions for a GLSurfaceView object. This class
 * must override the OpenGL ES drawing lifecycle methods:
 * <ul>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceCreated}</li>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onDrawFrame}</li>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceChanged}</li>
 * </ul>
 */
public class MyGLRenderer implements GvrView.StereoRenderer {

    private static final String TAG = "MyGLRenderer";
    private Triangle mTriangle;
    private Square mSquare;
    private Cube mCube;
    private Floor mFloor;

    public MyGLRenderer(Context context) {
        this.context = context;
    }

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] view = new float[16];
    private float[] camera = new float[16];

    private float[] headView = new float[16];
    private float[] modelViewProjection = new float[16];
    private float[] modelView = new float[16];
    private float[] modelFloor = new float[16];
    private float[] lightPosInEyeSpace = new float[4];


    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;
    private static final float CAMERA_Z = 5.0f;


    private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[]{0.0f, 2.0f, 0.0f, 1.0f};

    public Context context;


    private float mAngle;

    @Override
    public void onDrawEye(Eye eye) {

        float[] scratch = new float[16];

        // Draw background color

        checkGLError("colorParam");

        int intColor = Color.parseColor("#112233");
        GLES20.glClearColor(Color.red(intColor) / 255.0f,
                Color.green(intColor) / 255.0f,
                Color.blue(intColor) / 255.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);
        Matrix.multiplyMV(lightPosInEyeSpace, 0, view, 0, LIGHT_POS_IN_WORLD_SPACE, 0);

        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);

        mCube.draw(view, perspective, modelView, lightPosInEyeSpace, modelViewProjection);

        // Set modelView for the floor, so we draw floor in the correct location
        mFloor.draw(view, perspective,modelView, lightPosInEyeSpace, modelViewProjection);

        checkGLError("afterDraw");


    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        headTransform.getHeadView(headView, 0);
        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int i, int i1) {

    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        // Set the background frame color
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well.

        checkGLError("FloorRenderer program params");


//        mTriangle = new Triangle();
//        mSquare = new Square();
        mCube = new Cube(context);
        mFloor = new Floor(context);

        checkGLError("onSurfaceCreated");

        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        checkGLError("onReadyToDraw");


    }

    @Override
    public void onRendererShutdown() {

    }


//    @Override
//    public void onSurfaceChanged(GL10 unused, int width, int height) {
//        // Adjust the viewport based on geometry changes,
//        // such as screen rotation
//        GLES20.glViewport(0, 0, width, height);
//
//        float ratio = (float) width / height;
//
//        // this projection matrix is applied to object coordinates
//        // in the onDrawFrame() method
//        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
//
//    }

    /**
     * Utility method for compiling a OpenGL shader.
     * <p>
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type       - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     * <p>
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     * <p>
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGLError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.d(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    /**
     * Returns the rotation angle of the triangle shape (mTriangle).
     *
     * @return - A float representing the rotation angle.
     */
    public float getAngle() {
        return mAngle;
    }

    /**
     * Sets the rotation angle of the triangle shape (mTriangle).
     */
    public void setAngle(float angle) {
        mAngle = angle;
    }

}