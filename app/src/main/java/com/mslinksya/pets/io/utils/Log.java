package com.mslinksya.pets.io.utils;

public class Log {
    private static final String PROJECT_TAG = "| PETS-IO |";

    public static void d(String tag, String msg) {
        android.util.Log.d(PROJECT_TAG, tag + ": " + msg);
    }

    public static void w(String tag, String msg) {
        android.util.Log.w(PROJECT_TAG, tag + ": " + msg);
    }

    public static void w(String tag, String msg, Throwable tr) {
        android.util.Log.w(PROJECT_TAG, tag + ": " + msg, tr);
    }

    public static void e(String tag, String msg) {
        android.util.Log.e(PROJECT_TAG, tag + ": " + msg);
    }
}
