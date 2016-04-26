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
import java.util.Arrays;
import java.util.List;


/**
 * Classe di gestione e configurazione della Camera.
 */
@SuppressWarnings("deprecation")
public final class CameraView extends SurfaceView implements
        SurfaceHolder.Callback, PreviewCallback {

    private static final String TAG = "CameraView";
    static final private int NUM_BUFFERS = 5;

    static {
        System.loadLibrary("SdemAppJNI");
    }

    DrawView drawView;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Context mContext;
    private int mHeight;
    private int mWidth;
    private float[] cornersList = new float[0];
    private int[] nMarkers = new int[1];
    private int[] idList = null;
    private CameraHandlerThread mThread = null;

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

        nMarkers[0] = 0;
        drawView = (sdem.unimore.com.sdemapp.DrawView) ((Activity) mContext).findViewById(R.id.drawingSurface);
    }

    /**
     * Lancia una nuova istanza della Camera.
     */
    public void getCameraInstance() {
        newOpenCamera();
    }

    /**
     * Metodo standard per aprire una nuova istanza della camera.
     */
    private void oldOpenCamera() {
        try {
            mCamera = Camera.open(); // attempt to get a Camera instance
            mCamera.setPreviewCallbackWithBuffer(this);
            Log.i(TAG, "Instance created");
        } catch (Exception e) {
            Log.e(TAG, "Error getting Camera instance: " + e.getMessage());
        }
    }

    /**
     * Genera una nuova istanza della camera chiamando un nuovo thread di gestione, separatamente dal thread principale.
     */
    private void newOpenCamera() {
        if (mThread == null) {
            mThread = new CameraHandlerThread();
        }

        synchronized (mThread) {
            mThread.openCamera();
        }
    }

    /**
     * Overload metodo di SurfaceHolder.Callback.
     * Viene chiamato immediatamente dopo che la surface viene creata.
     * Al suo interno viene impostata la dimensione corretta per la preview della Camera e successivamente lanciata.
     *
     * @param holder holder surface
     */
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surface created");

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
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);

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

    /**
     * Imposta la dimensione del buffer pre-allocato nella coda dei buffer della preview.
     *
     * @param bufferSize dimensione del buffer
     */
    private void setupCallback(int bufferSize) {
        mCamera.setPreviewCallbackWithBuffer(this);
        for (int i = 0; i <= NUM_BUFFERS; ++i) {
            byte[] cameraBuffer = new byte[bufferSize];
            mCamera.addCallbackBuffer(cameraBuffer);
        }
    }

    /**
     * Overload metodo di SurfaceHolder.Callback
     * Viene chiamato nel momento in cui avviene un qualsiasi cambiamento strutturale alla surface.
     *
     * @param holder holder della surface
     * @param format formato della surface
     * @param width  nuova larghezza della surface
     * @param height nuova altezza della surface
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surface changed");

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

    /**
     * Overload metodo di Camera.PreviewCallback
     * Viene chiamato quando i frame di preview vengono mostrati. Questo callback viene invocato all'interno del thread in cui la camera viene lanciata.
     *
     * @param data   contenuti del preview frame
     * @param camera oggetto camera
     */
    public void onPreviewFrame(final byte[] data, Camera camera) {

        if (cornersList.length == 0) {
            cornersList = new float[nMarkers[0] * 8];
        }

        if (idList == null) {
            idList = new int[nMarkers[0]];
        }

        //DETECTION AR MARKER
        detectJNI(data, mHeight, mWidth, nMarkers, idList, cornersList);
        popupLogic(idList);

        if (nMarkers[0] == 0) {
            cornersList = new float[nMarkers[0] * 8];
            Arrays.fill(idList, 0);
        }

        ((Activity) mContext).runOnUiThread(new Runnable() {
            public void run() {
                drawView.drawCorners(cornersList, idList);
                invalidate();
            }
        });

        camera.addCallbackBuffer(data);
    }

    private boolean found1 = false;
    private boolean found2 = false;

    final static private int ID1 = 23;
    final static private int ID2 = 2;
    final static private int ID3 = 3;

    /**
     * Mostra i popup appropriati a seconda del marker rilevato
     * @param idList lista di ID
     */
    private void popupLogic(int[] idList) {
        int id;
        if (idList == null) {
            return;
        } else {
            id = idList[0];
        }

        //trovato ID1
        if (id == ID1) {
            if (!found1) {
                //showpopup1
                found1 = true;
            }
        }

        //Trovato ID2
        if (id == ID2) {
            if (found1) {
                //showpopup2
                found2 = true;
                return;
            } else {
                //showpopup -devi trovare il ID1
                return;
            }
        }

        //trovato ID3
        if (id == ID3) {
            if (found1 && found2) {
                //showpopup3 -hai trovato tutti i marker
            } else {
                if (!found2) {
                    if (!found1) {
                        //showpopup devi trovare ID1
                        return;
                    }
                    //showpopup -devi trovare ID2
                    return;
                }
            }
        }
    }

    /**
     * Overload metodo di SurfaceHolder.Callback.
     * Viene chiamato immediatamente dopo che la surface viene distrutta.
     *
     * @param holder holder della surface.
     */

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
        }
        mCamera = null;
    }

    /**
     * Metodo nativo di rilevamento del marker.
     *
     * @param data       Contenuto frame di preview
     * @param height     altezza frame
     * @param width      larghezza frame
     * @param nMarker    numero di markers rilevati
     * @param idList     vettore di ID dei marker rilevati
     * @param cornerList vettore di corners rilevati per ogni marker
     */
    private native void detectJNI(byte[] data, int height, int width, int[] nMarker, int[] idList, float[] cornerList);

    /**
     * Thread di gestione del ciclo di vita della Camera.
     * La camera viene lanciata da un thread differente rispetto al thread principale in modo da separarla dalle operazioni di gestione interfaccia.
     */
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

//    public static void runOnUiThread(Runnable runnable) {
//        final Handler UIHandler = new Handler(Looper.getMainLooper());
//        UIHandler.post(runnable);
//    }

}