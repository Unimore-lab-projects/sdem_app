package sdem.unimore.com.sdemapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Alessandro on 04/04/2016.
 */
public class DrawView extends SurfaceView implements SurfaceHolder.Callback {

    private Paint linePaint = new Paint();
    private Paint textPaint = new Paint();
//    private float[] cornersList = null;


    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);

        this.setBackgroundColor(Color.TRANSPARENT);
        this.setZOrderOnTop(true); //necessary
        getHolder().setFormat(PixelFormat.TRANSPARENT);

        linePaint.setColor(Color.RED);
        linePaint.setStrokeWidth(3);
        linePaint.setPathEffect(null);
        linePaint.setStyle(Paint.Style.STROKE);

        textPaint.setTextSize(40);
        textPaint.setColor(Color.RED);
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        if (corners != null) {
//            canvas.drawPath(drawMarkerContour(), linePaint);
//            Log.d("DrawView", "lines drawn");
//        } else {
//            Log.d("DrawView", "corners==Null");
//        }
    }

    public void drawCorners(float[] corners) {
        Path path = new Path();
        if (corners.length != 0 && corners != null) {
            path.moveTo(corners[0], corners[1]);
            path.lineTo(corners[2], corners[3]);
            path.lineTo(corners[4], corners[5]);
            path.lineTo(corners[6], corners[7]);
            path.lineTo(corners[0], corners[1]);

//            for (int i = 0; i < corners.length-1; ++i) {
//                if ((i % 4) == 0) {
//                    path.moveTo(corners[i], corners[i + 1]);
//                    continue;
//                }
//                path.lineTo(corners[i], corners[i + 1]);
//            }
        }
        Canvas canvas = getHolder().lockCanvas();
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawPath(path, linePaint);
        getHolder().unlockCanvasAndPost(canvas);
    }

//    public void setCorners(float[] corners) {
//        this.corners = corners;
//    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
//
//    class PanelThread extends Thread {
//        private SurfaceHolder _surfaceHolder;
//        private DrawView _panel;
//        private boolean _run = false;
//
//
//        public PanelThread(SurfaceHolder surfaceHolder, DrawView panel) {
//            _surfaceHolder = surfaceHolder;
//            _panel = panel;
//        }
//
//
//        public void setRunning(boolean run) { //Allow us to stop the thread
//            _run = run;
//        }
//
//
//        @Override
//        public void run() {
//            Canvas c;
//            while (_run) {     //When setRunning(false) occurs, _run is
//                c = null;      //set to false and loop ends, stopping thread
//
//
//                try {
//
//
//                    c = _surfaceHolder.lockCanvas(null);
//                    synchronized (_surfaceHolder) {
//
//                        c.drawPath(drawMarkerContour(), linePaint);
//                        //Insert methods to modify positions of items in onDraw()
//                        postInvalidate();
//                    }
//                } finally {
//                    if (c != null) {
//                        _surfaceHolder.unlockCanvasAndPost(c);
//                    }
//                }
//            }
//        }
//    }


}

