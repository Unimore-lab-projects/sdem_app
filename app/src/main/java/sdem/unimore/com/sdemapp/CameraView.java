package sdem.unimore.com.sdemapp;

import android.app.Activity;
import android.app.AlertDialog;
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
import java.util.Timer;
import java.util.TimerTask;


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
    private int previuosMarkersNumber = 0;
    private int[] idList = new int[0];
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
        if (mCamera != null) {
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

        //DETECTION AR MARKER
        detectJNI(data, mHeight, mWidth, nMarkers, idList, cornersList);


        ((Activity) mContext).runOnUiThread(new Runnable() {
            public void run() {
                popupLogic(idList);
                drawView.drawCorners(cornersList, idList);
                invalidate();
            }
        });
        if (nMarkers[0] != previuosMarkersNumber) {
            cornersList = new float[nMarkers[0] * 8];
            idList = new int[nMarkers[0]];
            previuosMarkersNumber = nMarkers[0];
        }

        camera.addCallbackBuffer(data);
    }


    final static private int ID1 = 23;
    final static private int ID2 = 3;
    final static private int ID3 = 5;

    private void showDialog(int ID, boolean correct) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        String messageFindNext = "Hai trovato il Marker corretto! Ora cerca il numero: " + ID;
        String messageRetry = "Devi trovare il marker " + ID;
        String hintID1 = "\nCercalo vicino ad un aeroplano";
        String hintID2 = "\nCercalo vicino a tanti cavalli";
        String hintID3 = "\nCercalo vicino ad un grosso 26";

        if (correct) {
            builder.setTitle("Congratulazioni");
            switch (ID) {
                case ID1: {
                    builder.setMessage(messageFindNext + hintID2);
                    break;
                }
                case ID2: {
                    builder.setMessage(messageFindNext + hintID3);
                    break;
                }
                case ID3: {
                    builder.setMessage("Hai trovato tutti i marker!");
                    break;
                }
            }
        } else {
            builder.setTitle("Riprova");
            switch (ID) {
                case ID1: {
                    builder.setMessage(messageRetry + hintID1);
                    break;
                }
                case ID2: {
                    builder.setMessage(messageRetry + hintID2);
                    break;
                }
                case ID3: {
                    builder.setMessage(messageRetry + hintID3);
                    break;
                }
            }
        }

        int TIME;

        if (startup) {        //messaggio di startup

            builder.setTitle("BENVENUTO");
            builder.setMessage("Devi trovare 3 marker nell'ordine corretto." +
                    "\nComincia dal Marker numero 23, cercalo vicino ad un aereo");
            TIME = 5000;
        } else {
            TIME = 2000;
        }

        final AlertDialog dlg = builder.create();
        dlg.show();

        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                dlg.dismiss();
                t.cancel();
            }
        }, TIME);
    }

    private boolean startup = true;
    private boolean foundID1 = false;
    private boolean foundID2 = false;
    private boolean foundID3 = false;

    /**
     * Mostra i popup appropriati a seconda del marker rilevato. vengono utilizzati i flag foundID
     * per non ripetere i messaggi di avviso piu di una volta.
     *
     * @param idList lista di ID
     */
    private void popupLogic(int[] idList) {
        int markerId;

        if (idList.length == 0) {
            //startup
            if (startup) { //mostra messaggio di benvenuto
                showDialog(ID1, false); //trova ID1
                startup = false; //non viene piu mostrato
            }
            return; //non fa nulla
        } else {
            markerId = idList[0]; //controlla solo il primo marker tra tutti i rilevati nel frame.
        }

        switch (markerId) {
            case ID1: { //23
                if (!foundID1) { //non ancora stato trovato
                    showDialog(ID2, true); //OK, vai a ID2
                    foundID1 = true;
                    break;
                } else {
                    break; //non fare niente
                }
            }
            case ID2: { //3
                if (foundID1) { //Se ID1 è già stato trovato
                    showDialog(ID3, true); //OK, vai a ID3
                    foundID2 = true;
                    break;
                } else { //devi trovare prima ID1
                    showDialog(ID1, false); //NO, cerca ID1 prima.
                    foundID1 = false;
                    break;
                }
            }
            case ID3: { //5
                if (!foundID3) { //se ID3 non è ancora stato trovato
                    if (foundID1 && foundID2) { //se ID1 e ID2 sono già stati trovati
                        showDialog(ID3, true); //congrats
                        foundID3 = true;
                        break;
                    } else {
                        if (!foundID2) { //ID2 non è stato trovato
                            if (!foundID1) { // ID1 non è stato trovato
                                showDialog(ID1, false); //trova ID1
                                break;
                            } else { // ID1 trovato
                                showDialog(ID2, false); //trova ID2
                                break;
                            }
                        }
                        break;
                    }
                }
                break;
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