package de.threateningcodecomments.routinetimer;

import android.util.Log;

class MyLog {
    public static final String DEFAULT_TAG = "myLog";

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

    /*public static void out(String message) {
        System.out.println(message);
    }*/
}
