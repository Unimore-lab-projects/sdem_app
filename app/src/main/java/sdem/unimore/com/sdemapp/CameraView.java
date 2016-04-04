package sdem.unimore.com.sdemapp;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

/**
 * Classe di gestione e configurazione della Camera.
 */
public final class CameraView extends SurfaceView implements
        SurfaceHolder.Callback, Runnable, PreviewCallback {

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private byte[] mBuffer = null;
    private boolean mThreadRun;
    private static final String TAG = "CameraView";
    private Context mContext;
    private float focalLenght;
    private int mHeight;
    private int mWidth;

    /**
     * Costruttore oggetto Camera
     *
     * @param context
     */
    public CameraView(Context context) {
        super(context);
        mContext = context;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void getCameraInstance() {
        try {
            mCamera = Camera.open(); // attempt to get a Camera instance
            mCamera.setPreviewCallbackWithBuffer(this);
            Log.i(TAG, "Instance created");
        } catch (Exception e) {
            Log.e(TAG, "Error getting Camera instance: " + e.getMessage());
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surface created");
        new Thread(this).start();

        // Set CameraView to the optimal camera preview size

        final Camera.Parameters params = mCamera.getParameters();
        final List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        final int screenWidth = ((View) getParent()).getWidth();
        int minDiff = Integer.MAX_VALUE;
        Camera.Size bestSize = null;

        focalLenght = params.getFocalLength();

        /*
        * Impostazione dimensione frame a seconda delle dimensioni ottimali e dell'orientamento
        */
        if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) {
            for (Camera.Size size : sizes) {
                final int diff = Math.abs(size.width - screenWidth);
                if (diff < minDiff) {
                    minDiff = diff;
                    bestSize = size;
                }
            }
        } else {
            mCamera.setDisplayOrientation(90);
            for (Camera.Size size : sizes) {
                final int diff = Math.abs(size.height - screenWidth);
                if (Math.abs(size.height - screenWidth) < minDiff) {
                    minDiff = diff;
                    bestSize = size;
                }
            }
        }
        final int previewWidth = bestSize.width;
        final int previewHeight = bestSize.height;
        mHeight = previewHeight;
        mWidth = previewWidth;

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = previewHeight;
        layoutParams.width = previewWidth;
        setLayoutParams(layoutParams);


        // FORMATO PREVIEW
        params.setPreviewFormat(ImageFormat.NV21);
        params.setPreviewSize(previewWidth, previewHeight);
        mCamera.setParameters(params);

        //buffer di uscita
        int size = previewWidth * previewHeight *
                ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
        mBuffer = new byte[size];
        mCamera.addCallbackBuffer(mBuffer);

        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            Log.i(TAG, "preview started");
        } catch (IOException e) {
            Log.e(TAG, "Error setting camera preview: " + e.getMessage());
        }

    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surface changed");

        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        //TO DO

        // start preview with new settings
        try {
            mCamera.setPreviewCallback(this);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            Log.i(TAG, "surface changed");
        } catch (Exception e) {
            Log.e(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public void onPreviewFrame(byte[] data, Camera camera) {
        synchronized (this) {
            CameraView.this.notify();
            mBuffer = data;
        }
    }

    private ImageView imageView = null;
    Bitmap bmp;
    float[] cornersList = null;
    private TextView idText = null;
    private int[] ids = null;
    public void run() {
        Log.i(TAG, "frame processing thread started");
        imageView = (ImageView) ((Activity) mContext).findViewById(R.id.imageView);
        imageView.setMaxHeight(mHeight);
        imageView.setMaxWidth(mWidth);

        idText = (TextView) ((Activity) mContext).findViewById(R.id.textID);
        mThreadRun = true;
        cornersList = new float[8];
        ids = new int[2];
        while (mThreadRun) {
            synchronized (this) {
                try {
                    this.wait();
                    detectAndDrawMarkersJNI(mBuffer, mHeight, mWidth, cornersList, ids);

//                    ByteArrayOutputStream out = new ByteArrayOutputStream();
//                    YuvImage yuvImage = new YuvImage(mBuffer,ImageFormat.NV21, mWidth, mHeight, null);
//                    yuvImage.compressToJpeg(new Rect(0, 0, mWidth, mHeight), 50, out);
//                    byte[] imageBytes = out.toByteArray();
//                    bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                    Log.v(TAG, "id: "+ids[0]+" first corner: "+cornersList[0] );

                } catch (InterruptedException e) {
                    Log.e(TAG, "Error in frame processing thread: " + e.getMessage());
                }

                // thread di aggiornamento della UI
                Utils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        idText.setText("ID: "+String.valueOf(ids[0]));
//                        imageView.setImageBitmap(bmp);
                    }

                });
            }
            // Request a new frame from the camera by putting
            // the buffer back into the queue
            mCamera.addCallbackBuffer(mBuffer);
        }

        Log.i(TAG, "frame processing thread loop ended");
        mHolder.removeCallback(this);
        mCamera.stopPreview();
        mCamera.setPreviewCallback(null);
        mCamera.release();
        mCamera = null;
        Log.i(TAG, "camera released");
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mThreadRun = false;
    }

    private native void provaJNI(byte[] data);

    private native void detectAndDrawMarkersJNI(byte[] data, int height, int width, float[] markerList, int[] ids);


    static {
        System.loadLibrary("SdemAppJNI");
    }

}