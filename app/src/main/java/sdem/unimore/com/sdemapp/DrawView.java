package sdem.unimore.com.sdemapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class DrawView extends SurfaceView implements SurfaceHolder.Callback {

    private Paint linePaint = new Paint();
    private Paint textPaint = new Paint();

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);


        this.setBackgroundColor(Color.TRANSPARENT);
        this.setZOrderOnTop(true); //necessary
        getHolder().setFormat(PixelFormat.TRANSPARENT);

        linePaint.setColor(getColor(context, R.color.colorAccent));
        linePaint.setStrokeWidth(3);
        linePaint.setPathEffect(null);
        linePaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setColor(getColor(context, R.color.colorAccent));
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(50);
        setWillNotDraw(false);
    }

    public void drawCorners(float[] corners, int[] idList) {
        Path path = new Path();
        Canvas canvas = getHolder().lockCanvas();
        if (canvas != null) {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            if (corners.length > 0 && corners != null) {

                path.moveTo(corners[0], corners[1]);
                path.lineTo(corners[2], corners[3]);
                path.lineTo(corners[4], corners[5]);
                path.lineTo(corners[6], corners[7]);
                path.lineTo(corners[0], corners[1]);


                if (idList.length > 0 && idList != null) {
                    canvas.drawText(Integer.toString(idList[0]), corners[0], corners[1], textPaint);
                }
                canvas.drawPath(path, linePaint);
            }
            getHolder().unlockCanvasAndPost(canvas);
        }
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

    public static final int getColor(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, id);
        } else {
            return context.getResources().getColor(id);
        }
    }


}

