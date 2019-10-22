package cse.buffalo.edu;

import android.app.Application;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private Triangle mTriangle;
    private ObjectDrawer object;
    private Context appcontext;
    public MyGLRenderer(Context appcontext){
        this.appcontext=appcontext;
    }
    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.f);
        object.draw();
        //mTriangle.draw();
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
       // mTriangle.onsufchng(width,height);
        object.onSurfaceChanged(width,height);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig) {
       // mTriangle = new Triangle();
        object= new ObjectDrawer(appcontext);
        object.onSurfaceCreated();
    }
}
