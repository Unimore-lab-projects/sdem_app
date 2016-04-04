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
    private Path[] markers = null;
    private float[] corners = null;


    public DrawView(Context context) {
        super(context);
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setZOrderOnTop(true); //necessary
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        getHolder().addCallback(this);

        linePaint.setColor(Color.RED);
        linePaint.setStrokeWidth(3);
        linePaint.setPathEffect(null);
        linePaint.setStyle(Paint.Style.STROKE);
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void drawMarkerContour(SurfaceHolder holder) {
        Path myPath = new Path();

        if (corners != null) {
            myPath.moveTo(corners[0], corners[1]);
            myPath.lineTo(corners[2], corners[3]);
            myPath.lineTo(corners[4], corners[5]);
            myPath.lineTo(corners[6], corners[7]);
        }

        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            Log.e("DrawView", "Cannot draw onto the canvas as it's null");
        } else {
            canvas.drawPath(myPath, linePaint);
            holder.unlockCanvasAndPost(canvas);
        }

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
}
