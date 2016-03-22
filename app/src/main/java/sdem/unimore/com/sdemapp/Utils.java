package sdem.unimore.com.sdemapp;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by Alessandro on 22/03/2016.
 */
public class Utils {

    public static void runOnUiThread(Runnable runnable){
        final Handler UIHandler = new Handler(Looper.getMainLooper());
        UIHandler .post(runnable);
    }
}


