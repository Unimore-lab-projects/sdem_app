package sdem.unimore.com.sdemapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Alessandro on 21/03/2016.
 */
public class DrawView extends SurfaceView implements SurfaceHolder.Callback {

    private Paint textPaint = new Paint();

    public DrawView(Context context) {
        super(context);
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setZOrderOnTop(true); //necessary
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        getHolder().addCallback(this);

        textPaint.setARGB(255, 200, 0, 0 );
        textPaint.setTextSize(60);
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas){
        canvas.drawText("HELLO WORLD!", 50, 50, textPaint);
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
