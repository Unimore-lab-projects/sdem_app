package sdem.unimore.com.sdemapp;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.List;


/**
 * Classe di gestione e configurazione della Camera.
 */
@SuppressWarnings("deprecation")
public final class CameraView extends SurfaceView implements
        SurfaceHolder.Callback, PreviewCallback {

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private static final String TAG = "CameraView";
    private Context mContext;
    private int mHeight;
    private int mWidth;
    private float[] cornersList = new float[0];
    private int[] nMarkers = new int[1];
    private int[] idList = null;
    DrawView drawView;


    /**
     * Costruttore oggetto Camera
     *
     * @param context Context
     */
    public CameraView(Context context) {
        super(context);
        mContext = context;
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        nMarkers[0] = 1;
    }

    public void getCameraInstance() {
        newOpenCamera();
    }

    private void oldOpenCamera() {
        try {
            mCamera = Camera.open(); // attempt to get a Camera instance
            mCamera.setPreviewCallbackWithBuffer(this);
            Log.i(TAG, "Instance created");
        } catch (Exception e) {
            Log.e(TAG, "Error getting Camera instance: " + e.getMessage());
        }
    }

    private void newOpenCamera() {
        if (mThread == null) {
            mThread = new CameraHandlerThread();
        }

        synchronized (mThread) {
            mThread.openCamera();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surface created");
//        new Thread(this).start();

        final Camera.Parameters params = mCamera.getParameters();
        final List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        final int screenWidth = ((View) getParent()).getWidth();
        int minDiff = Integer.MAX_VALUE;
        Camera.Size bestSize = null;

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
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        mCamera.setParameters(params);

        //buffer di uscita
        int size = previewWidth * previewHeight *
                ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
        setupCallback(size);

        // Esecuzione preview
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            Log.i(TAG, "preview started");
        } catch (IOException e) {
            Log.e(TAG, "Error setting camera preview: " + e.getMessage());
        }

    }

    static final private int NUM_BUFFERS = 10;

    private void setupCallback(int bufferSize) {
        mCamera.setPreviewCallbackWithBuffer(this);
        for (int i = 0; i <= NUM_BUFFERS; ++i) {
            byte[] cameraBuffer = new byte[bufferSize];
            mCamera.addCallbackBuffer(cameraBuffer);
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
        drawView = (sdem.unimore.com.sdemapp.DrawView) ((Activity) mContext).findViewById(R.id.drawingSurface);


        if (cornersList.length == 0 ) {
            cornersList = new float[nMarkers[0] * 8];
        }
        if(idList == null){
            idList = new int[nMarkers[0]];
        }

        detectJNI(data, mHeight, mWidth, nMarkers, idList, cornersList);
        if (nMarkers[0] == 0) {
            cornersList = new float[nMarkers[0] * 8];
            idList = null;
        }

        ((Activity) mContext).runOnUiThread(new Runnable() {
            public void run() {
                drawView.drawCorners(cornersList, idList);
                postInvalidate();
            }
        });

        camera.addCallbackBuffer(data);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
        }
        mCamera = null;
    }


    private native void detectJNI(byte[] data, int height, int width, int[] nMarker, int[] idList, float[] cornerList);

    static {
        System.loadLibrary("SdemAppJNI");
    }

    private CameraHandlerThread mThread = null;

    private class CameraHandlerThread extends HandlerThread {
        Handler mHandler = null;

        CameraHandlerThread() {
            super("CameraHandlerThread");
            start();
            mHandler = new Handler(getLooper());
        }

        synchronized void notifyCameraOpened() {
            notify();
        }

        void openCamera() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    oldOpenCamera();
                    notifyCameraOpened();
                }
            });
            try {
                wait();
            } catch (InterruptedException e) {
                Log.w(TAG, "wait was interrupted");
            }
        }
    }

}