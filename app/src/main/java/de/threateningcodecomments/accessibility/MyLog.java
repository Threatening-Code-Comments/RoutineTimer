package de.threateningcodecomments.accessibility;

import android.util.Log;

public class MyLog {
    public static final String DEFAULT_TAG = "myLog";
    public static final String DEFAULT_FIREBASE_TAG = "myFirebase";


    public static void d(String message) {
        if (message == null) {
            Log.d(DEFAULT_TAG, "null value");
        } else {

            Log.d(DEFAULT_TAG, message);
        }
    }

    public static void d(Object o) {
        String message = String.valueOf(o);
        d(message);
    }

    public static void d(Object[] array) {
        MyLog.d("length of the Array is: " + array.length);
        for (Object v : array) {
            MyLog.d("value: " + v.toString());
        }
    }

    public static void e(String message, Throwable tr) {
        Log.w(DEFAULT_TAG, message, tr);
    }


    public static void f(String str) {
        Log.d(DEFAULT_FIREBASE_TAG, str);
    }

    public static void fe(String message, Throwable tr) {
        Log.w(DEFAULT_FIREBASE_TAG, message, tr);
    }
}
