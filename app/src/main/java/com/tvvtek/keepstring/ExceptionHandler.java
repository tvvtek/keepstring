package com.tvvtek.keepstring;


import android.util.Log;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Log.d("KeepString", "catched_error=", throwable);
    }
}