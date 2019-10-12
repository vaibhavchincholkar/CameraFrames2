package cse.buffalo.edu;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private Triangle mTriangle;
    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.f);
       // GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig) {
       // mTriangle = new Triangle();
      //  int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
    }
}
