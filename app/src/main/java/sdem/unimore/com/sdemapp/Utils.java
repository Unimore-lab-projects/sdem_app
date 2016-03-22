package sdem.unimore.com.sdemapp;

import android.os.Handler;
import android.os.Looper;

import java.util.Random;

/**
 * Created by Alessandro on 22/03/2016.
 */
public class Utils {

    public static void runOnUiThread(Runnable runnable){
        final Handler UIHandler = new Handler(Looper.getMainLooper());
        UIHandler .post(runnable);
    }


    /**
     * calcola e ritorna la distanza del marker dalla telecamera
     * @param knownWidth dimensione del marker conosciuta
     * @param focalLenght distanza focale in mm
     * @param points 2 vertici consecutivi del marker
     * @return
     */
    public static double getDistanceFromMarker(int knownWidth, float focalLenght, float[] points){

        float x1,x2,y1,y2;

        x1=points[0];
        x2=points[2];
        y1=points[1];
        y2=points[3];

        double perWidth=Math.sqrt((y2-y1)+(x2-x1));

        return (knownWidth*focalLenght)/perWidth;
    }


}


