package com.qualaroo;

import android.util.Log;

public final class QualarooLogger {

    private static final String TAG = "QularooSDK";

    private static Logger logger = new Logger();
    private static boolean isDebugMode = false;

    static void enableLogging() {
        logger = new AndroidLogger();
    }

    static void setDebugMode() {
        isDebugMode = true;
    }

    public static void debug(String message, Object... args) {
        debug(String.format(message, args));
    }

    public static void debug(String message) {
        if (isDebugMode) {
            Log.d(TAG, message);
        }
    }

    public static void error(String message, Throwable throwable) {
        logger.e(message, throwable);
    }

    public static void error(String message) {
        logger.e(message);
    }

    public static void info(String message) {
        logger.i(message);
    }

    public static void info(String message, Object... args) {
        logger.i(String.format(message, args));
    }

    private static class Logger {
        void i(String message){}
        void d(String message){}
        void e(String message){}
        void e(String message, Throwable throwable){}
    }

    private final static class AndroidLogger extends Logger {

        @Override public void i(String message) {
            Log.i(TAG, message);
        }

        @Override public void d(String message) {
            Log.d(TAG, message);
        }

        @Override public void e(String message, Throwable throwable) {
            Log.e(TAG, message, throwable);
        }

        @Override void e(String message) {
            Log.e(TAG, message);
        }
    }

}
