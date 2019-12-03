package cse.buffalo.edu;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL10;
public class ObjectDrawer {
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;
    private FloatBuffer mNormalsBuffer;
    private IntBuffer mIndexBuffer;
    ArrayList<Float> vertices;
    ArrayList<Float> vertexTextures;
    ArrayList<Float> vertexNormals;
    ArrayList<Integer> faces;
    Context context;

    private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private int mTextureDataHandle;
    private int mProgramHandle;
    /** This will be used to pass in the transformation matrix. */
    private int mMVPMatrixHandle;
    /** This will be used to pass in the modelview matrix. */
    private int mMVMatrixHandle;
    /** This will be used to pass in the texture. */
    private int mTextureUniformHandle;
    /** This will be used to pass in model position information. */
    private int mPositionHandle;
    /** This will be used to pass in model texture coordinate information. */
    private int mTextureCoordinateHandle;
    /** Size of the position data in elements. */
    private final int mPositionDataSize = 3;
    /** Size of the texture coordinate data in elements. */
    private final int mTextureCoordinateDataSize = 2;

    public ObjectDrawer(Context context)  {
        this.context=context;
        ObjectParser p= null;
        try {
            p = new ObjectParser("/storage/emulated/0/Np/cube.obj");
            Log.e("Parsing done","object parsed");
        } catch (IOException e) {
            Log.e("Error in","object parsing maybe in the file location or access permission");
            Log.e("error: ",""+e.getMessage());
        }
        vertices=p.getVertexs();
        vertexTextures=p.getVertexTex();
        faces=p.getFaces();
        vertexNormals=p.getNormals();

        float[] vertex = new float[vertices.size()];
        for(int i=0;i<vertices.size();i++){
            vertex[i]= (float) (vertices.get(i)*0.1); //we need to multiply by a factor to get correct size of the object
        }
        float[] verTexture = new float[vertexTextures.size()];
        for(int i=0;i<vertexTextures.size();i++){
            verTexture[i]=vertexTextures.get(i);
        }
        float[] normals = new float[vertexNormals.size()];
        for(int i=0;i<vertexNormals.size();i++){
            normals[i]=vertexNormals.get(i);
        }
        int[] faceTriangles = new int[faces.size()];
        for(int i=0;i<faces.size();i++){
            faceTriangles[i]= i;
        }
        mVertexBuffer =  getFloatBuffer(vertex);
        mTextureBuffer =getFloatBuffer(verTexture);
        mNormalsBuffer = getFloatBuffer(normals);
        mIndexBuffer =  getIntBuffer(faceTriangles);
    }

    public void onSurfaceCreated()
    {
        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        // Position the eye in front of the origin.
        /**
         * The eye, or the position of the viewer;
         *
         * The center, or the point where we the camera aims;
         *
         * The up, which defines the direction of the up for the viewer.
         *
         * The defaults in OpenGL are: the eye at (0, 0, -1); the center at
         * (0, 0, 0) and the up is given by the positive direction of the Oy axis (0, 1, 0).*/
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = -2.0f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = 0.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

     /*  final float eyeX = 0.99f;
        final float eyeY = 0.02f;
        final float eyeZ = -0.110f;

        // We are looking toward the distance
        final float lookX = -0.022f;
        final float lookY = 0.99f;
        final float lookZ = -0.036f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = -0.111f;
        final float upY = 0.033f;
        final float upZ = 0.99f;*/

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
      /*  mViewMatrix= new float[]{0.99f, 0.02f, 0.110f, 0.94f,
                                -0.022f, 0.99f, -0.036f, 0.63f,
                                -0.111f, 0.033f, 0.9f, -6.277f,
                                0, 0, 0, 1.0f};*/
        final String vertexShader = getVertexShader();
        final String fragmentShader = getFragmentShader();
        final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
        mProgramHandle = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                new String[] {"a_Position", "a_TexCoordinate"});
        // Load the texture
        mTextureDataHandle = TextureHelper.loadTexture(context, R.drawable.cb);
    }

    public void onSurfaceChanged( int width, int height)
    {
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);
        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        /**
         * viewing angle or field of view (usually abbreviated as FOV);
         *
         * aspect
         *
         * near and far
         * */
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 1000.0f;
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    public void draw(){
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        // Set our per-vertex lighting program.
        GLES20.glUseProgram(mProgramHandle);
        // Set program handles for cube drawing.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix");
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");
        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);
        // Draw some cubes.
        Matrix.setIdentityM(mModelMatrix, 0);
        float x=0;
        x=(float) MainActivity.getXcord();
        float y= 0;
        y=(float)MainActivity.getYcord();
        float z= 0;
        z=(float)MainActivity.getZcord();
       // Matrix.translateM(mModelMatrix, 0, 1.0f,2.0f , 0.0f);
        Log.d("X and Y translation"," "+x+" "+y);
        Matrix.translateM(mModelMatrix, 0, x,y , z);
       // Matrix.translateM(mModelMatrix, 0, 823.8324277422465f,-978.3378398992778f , 0.0f);
        float[] rec= new float[]{-0.9627986438848656f, -0.01570760665841914f, 0.2697629374590367f, 1227.69832379624f,
                0.1288086509577005f, -0.9042681096959586f, 0.4070718821355203f, -968.8779629127691f,
                0.2375438965158251f, 0.4266760561362237f, 0.8726513853470451f, -1171.441600292768f,
                0, 0, 0, 1};
       // Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, rec, 0);

        Matrix.rotateM(mModelMatrix, 0,  angleInDegrees, 1.0f, 1.0f, 0.0f);
        drawCube();
    }
    private void drawCube()
    {
        // Pass in the position information
        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                0, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // Pass in the texture coordinate information
        mTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
                0, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        // Draw the cube.
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mIndexBuffer.limit(), GLES20.GL_UNSIGNED_INT, mIndexBuffer);
    }

    private FloatBuffer getFloatBuffer(float[] x) {
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(x.length * Float.BYTES); //4 bytes per float
        byteBuf.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = byteBuf.asFloatBuffer();
        buffer.put(x);
        buffer.position(0);
        return buffer;
    }
    private IntBuffer getIntBuffer(int[] x) {
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(x.length * Float.BYTES); //4 bytes per float
        byteBuf.order(ByteOrder.nativeOrder());
        IntBuffer buffer = byteBuf.asIntBuffer();
        buffer.put(x);
        buffer.position(0);
        return buffer;
    }
    protected String getVertexShader() {
        return RawResourceReader.readTextFileFromRawResource(context, R.raw.per_pixel_vertex_shader);
    }
    protected String getFragmentShader() {
        return RawResourceReader.readTextFileFromRawResource(context, R.raw.per_pixel_fragment_shader);
    }
}
