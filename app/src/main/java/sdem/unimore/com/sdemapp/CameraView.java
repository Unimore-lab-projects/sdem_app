package sdem.unimore.com.sdemapp;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.*;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.List;

/**
 * Classe di gestione e configurazione della Camera.
 */
public final class CameraView extends SurfaceView implements
        SurfaceHolder.Callback, Runnable, PreviewCallback {

    SurfaceHolder mHolder;
    Camera mCamera;
    byte[] mBuffer = null;
    boolean mThreadRun;

    /**
     * Costruttore oggetto Camera
     * @param context
     * @param attrs
     */
    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * Metodo di lancio della Camera. chiamato dalla activity genitore dopo aver impostato il contentView su CameraView
     */
    void openCamera() {
        mCamera = Camera.open();
        mCamera.setPreviewCallbackWithBuffer(this);
    }

    /**
     * Implementazione interfaccia SurfaceView.
     * Vengono impostate le dimensioni della preview della camera a seconda del dispositivo
     * Inoltre viene creato il buffer per i frame della preview
     * @param holder
     */
    public void surfaceCreated(SurfaceHolder holder) {
        new Thread(this).start();

        // Set CameraView to the optimal camera preview size

        final Camera.Parameters params = mCamera.getParameters();
        final List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        final int screenWidth = ((View) getParent()).getWidth();
        int minDiff = Integer.MAX_VALUE;
        Camera.Size bestSize = null;


        if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) {
            // Find the camera preview width that best matches the
            // width of the surface.
            for (Camera.Size size : sizes) {
                final int diff = Math.abs(size.width - screenWidth);
                if (diff < minDiff) {
                    minDiff = diff;
                    bestSize = size;
                }
            }
        } else {
            // Find the camera preview HEIGHT that best matches the 
            // width of the surface, since the camera preview is rotated.
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

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = previewHeight;
        layoutParams.width = previewWidth;
        setLayoutParams(layoutParams);

        params.setPreviewFormat(ImageFormat.NV21);
        params.setPreviewSize(previewWidth, previewHeight);
        mCamera.setParameters(params);

        //buffer di uscita
        int size = previewWidth * previewHeight *
                ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
        mBuffer = new byte[size];
        mCamera.addCallbackBuffer(mBuffer);

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Implementazione interfaccia SurfaceView.
     * @param holder
     * @param format
     * @param width
     * @param height
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    /**
     * Implementazione interfaccia SurfaceView.
     * @param data
     * @param camera
     */
    public void onPreviewFrame(byte[] data, Camera camera) {
        CameraView.this.notify();
    }

    /**
     * esecuzione del thread. I frame vengono salvati su mBuffer e processati da processFrame
     */
    public void run() {
        mThreadRun = true;
        while (mThreadRun) {
            synchronized (this) {
                try {
                    this.wait();
                    //processFrame(mBuffer); // PROCESSING DEI FRAME VERSO ARUCO
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // Request a new frame from the camera by putting 
            // the buffer back into the queue
            mCamera.addCallbackBuffer(mBuffer);
        }

        mHolder.removeCallback(this);
        mCamera.stopPreview();
        mCamera.setPreviewCallback(null);
        mCamera.release();
        mCamera = null;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mThreadRun = false;
    }
}