package sdem.unimore.com.sdemapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Alessandro on 04/04/2016.
 */
public class DrawView extends SurfaceView implements SurfaceHolder.Callback {

    private Paint linePaint = new Paint();
    private float[] corners = null;


    public DrawView(Context context) {
        super(context);
        getHolder().addCallback(this);

        this.setBackgroundColor(Color.TRANSPARENT);
        this.setZOrderOnTop(true); //necessary
        getHolder().setFormat(PixelFormat.TRANSPARENT);

        linePaint.setColor(Color.RED);
        linePaint.setStrokeWidth(3);
        linePaint.setPathEffect(null);
        linePaint.setStyle(Paint.Style.STROKE);
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        corners[0]=100;
//        corners[1]=200;

        if (corners != null) {
            canvas.drawPath(drawMarkerContour(), linePaint);
        } else {
            Log.d("DrawView", "corners==Null");
        }
    }

    public Path drawMarkerContour() {
        Path path = new Path();
        for (int i = 0; i < corners.length; ++i) {
            if ((i % 4) == 0) {
                path.moveTo(i, i + 1);
                continue;
            }
            path.lineTo(i, i + 1);
        }
        return path;
    }

    public void setCorners(float[] corners) {
        this.corners = corners;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    class PanelThread extends Thread {
        private SurfaceHolder _surfaceHolder;
        private DrawView _panel;
        private boolean _run = false;


        public PanelThread(SurfaceHolder surfaceHolder, DrawView panel) {
            _surfaceHolder = surfaceHolder;
            _panel = panel;
        }


        public void setRunning(boolean run) { //Allow us to stop the thread
            _run = run;
        }


        @Override
        public void run() {
            Canvas c;
            while (_run) {     //When setRunning(false) occurs, _run is
                c = null;      //set to false and loop ends, stopping thread


                try {


                    c = _surfaceHolder.lockCanvas(null);
                    synchronized (_surfaceHolder) {

                        c.drawPath(drawMarkerContour(), linePaint);
                        //Insert methods to modify positions of items in onDraw()
                        postInvalidate();
                    }
                } finally {
                    if (c != null) {
                        _surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
    }


}

