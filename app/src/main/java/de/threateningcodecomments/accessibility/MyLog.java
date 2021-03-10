package de.threateningcodecomments.accessibility;

import android.util.Log;
import android.widget.Toast;

import de.threateningcodecomments.routinetimer.MainActivity;
import de.threateningcodecomments.routinetimer.SettingsFragment;

public class MyLog {
    public static final String DEFAULT_TAG = "myLog";
    public static final String DEFAULT_FIREBASE_TAG = "myFirebase";


    public static void d(String message) {
        //nice idea, but it doesn't work
        /*if (SettingsFragment.Companion.getPreferences().getDev().getDebug()) {*/
            if (message == null) {
                Log.d(DEFAULT_TAG, "null value");
            } else {

                Log.d(DEFAULT_TAG, message);
            }
        /*}*/
    }

    public static void t(String message) {
        if (SettingsFragment.Companion.getPreferences().getDev().getDebug())
            Toast.makeText(MainActivity.instance, message, Toast.LENGTH_SHORT).show();
    }

    public static void d(Object o) {
        String message = String.valueOf(o);
        d(message);
    }


    public static void f(String str) {
        Log.d(DEFAULT_FIREBASE_TAG, str);
    }

    public static void fe(String message, Throwable tr) {
        Log.w(DEFAULT_FIREBASE_TAG, message, tr);
    }
}
