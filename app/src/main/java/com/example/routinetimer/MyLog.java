package com.example.routinetimer;

import android.util.Log;

class MyLog {
    public static final String DEFAULT_TAG = "myLog";

    public static void d(String message) {
        Log.d(DEFAULT_TAG, message);
    }

    public static void d(Object o) {
        String message = o.toString();
        d(message);
    }

    public static void d(float[] array) {
        MyLog.d("length of the Array is: " + array.length);
        for (float v : array) {
            if (v == 0) MyLog.d("value is null!");
            MyLog.d("value: " + v);
        }
    }

    public static void out(String message) {
        System.out.println(message);
    }
}
