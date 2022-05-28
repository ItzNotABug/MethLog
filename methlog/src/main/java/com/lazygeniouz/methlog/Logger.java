package com.lazygeniouz.methlog;

import android.util.Log;

public class Logger {
    public static void logInfo(String className, String methodName, long timeTaken) {
        String message = String.format("%s() completed in %2sms.", methodName, timeTaken);
        Log.i("MethLog", String.format("⇢ %s.%2s", className, message));
    }
}