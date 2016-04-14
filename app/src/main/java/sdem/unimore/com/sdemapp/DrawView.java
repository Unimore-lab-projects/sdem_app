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

import java.util.ArrayList;
import java.util.List;


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
        linePaint.setStrokeWidth(5);
        linePaint.setPathEffect(null);
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(getColor(context, R.color.colorPrimaryDark));
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(100);
        setWillNotDraw(false);
    }

    public void drawCorners(float[] corners, int[] idList) {
        Path path = new Path();
        Path textPath = new Path();
        Canvas canvas = getHolder().lockCanvas();
        List<Float> resX, resY;
        float sumX, sumY;
        if (canvas != null) {
            resX = new ArrayList<>();
            resY = new ArrayList<>();

            canvas.drawColor(0, PorterDuff.Mode.CLEAR);

            if (corners.length > 0) {
                for (int i = 0; i < corners.length; i = i + 8) {
                    path.moveTo(corners[i], corners[i + 1]);
                    sumX = corners[i];
                    sumY = corners[i + 1];

                    for (int j = i + 2; j < i + 8; j = j + 2) {
                        path.lineTo(corners[j], corners[j + 1]);
                        sumX = sumX+corners[j];
                        sumY = sumX+corners[j + 1];
                    }
                    path.lineTo(corners[i], corners[i + 1]);

                    resX.add(sumX / 4);
                    resY.add(sumY / 4);
                }

                canvas.drawPath(path, linePaint);

                if (idList.length > 0) {
                    canvas.drawText(Integer.toString(idList[0]), corners[0], corners[1], textPaint);
                }
                canvas.drawTextOnPath("99", textPath, resX.get(0), resY.get(0), textPaint);
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

    @SuppressWarnings("deprecation")
    public static int getColor(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, id);
        } else {

            return context.getResources().getColor(id);
        }
    }


}

