package cse.buffalo.edu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ColorDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static cse.buffalo.edu.R.drawable.ic_launcher_foreground;

public class MainActivity extends AppCompatActivity {
    private TextureView textureView;
    private String cameraId;
    private Size imageDimension;
    protected CameraDevice cameraDevice;
    private static final String TAG = "AndroidCameraApi";
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    protected CaptureRequest.Builder captureRequestBuilder;
    protected CameraCaptureSession cameraCaptureSessions;
    protected ImageReader imageReader;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private HandlerThread mCheckThread;
    private Handler mCheckHandler;
    int cnt=0;
    GLSurfaceView mGLView;
    FrameLayout fm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGLView = new GLSurfaceView(this);
        mGLView.setEGLConfigChooser(8,8,8,8,16,0);
        mGLView.setEGLContextClientVersion(2);
        mGLView.setRenderer(new MyGLRenderer(this));
        mGLView.setZOrderOnTop(true);
        mGLView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mGLView.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        //addContentView(mGLView,lp);
        //GlView will be added to fm onResume method
        fm= findViewById(R.id.mainFrameLayout);
        textureView =  findViewById(R.id.texture);
        textureView.setSurfaceTextureListener(textureListener);
        mCheckThread = new HandlerThread("CheckHandler");
        mCheckThread.start();
        mCheckHandler = new Handler(mCheckThread.getLooper());
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            //open your camera here
            openCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(MainActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }
        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    protected void createCameraPreview() {
        try {
            /**Captures frames from an image stream as an OpenGL ES texture.
             * */
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            /**A Surface is generally created by or from a consumer of image buffers (such as a SurfaceTexture, MediaRecorder, or Allocation),
             * and is handed to some kind of producer (such as OpenGL, MediaPlayer, or CameraDevice) to draw into.
             * */
            Surface surface = new Surface(texture);
            /**
            * Create a CaptureRequest.Builder for new capture requests, initialized with template for a target use case.
             * The settings are chosen to be the best options for the specific camera device, so it is not recommended to reuse
              * the same request for a different camera device;
             * create a builder specific for that device and template and override the settings as desired, instead.
            * */
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
           // captureRequestBuilder.addTarget(imageReader.getSurface());
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void updatePreview() {
        if(null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), new CameraCaptureSession.CaptureCallback(){
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Bitmap bt = textureView.getBitmap();
                  /**bitmap logic
                   * */
                    //TODO add socket code here after getting bitmap image
                    /**Currently I am compressing the image and storing it on internal storage you need to
                     * trasmit this image to edge server.
                     * NOTE: keep in mind that you can not directly do it over here so do it in Async task*/
                    File myFile = new File(getExternalFilesDir("NPP"), "vaibhav"+cnt+".jpg");
                    cnt++;
                    FileOutputStream fos = null;
                    try {
                        myFile.createNewFile();
                        fos = new FileOutputStream(myFile);
                        // Use the compress method on the BitMap object to write image to the OutputStream
                        bt.compress(Bitmap.CompressFormat.JPEG, 20, fos);
                        //TODO call your async task here you could use bitmap as itor path which I am using
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bt.compress(Bitmap.CompressFormat.JPEG,20,byteArrayOutputStream);
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, byteArrayOutputStream);

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if(fos!=null) fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    /**Bitmap logic ends here*/
                }
            }, mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
                mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        fm.addView(mGLView,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        mGLView.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }
    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private class ClientTask extends AsyncTask<ByteArrayOutputStream, Void, Void> {

        private static final int PORT = 5001;
        private static final String IP = "192.168.1.26";
        @Override
        protected Void doInBackground(ByteArrayOutputStream... byteArrayOutputStreams) {
            try {

                Socket socket = new Socket(InetAddress.getByName(IP), PORT);


                ByteArrayOutputStream msgToSend = byteArrayOutputStreams[0];
                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(outputStream);

                byte[] byteArray = msgToSend.toByteArray();
                int size = byteArray.length;
                dos.writeInt(size);
                //int offset = 0;
                //while(offset<size) {
                dos.write(byteArray, 0, size);
                //}
                dos.flush();
                socket.close();
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return null;
        }
    }

}
