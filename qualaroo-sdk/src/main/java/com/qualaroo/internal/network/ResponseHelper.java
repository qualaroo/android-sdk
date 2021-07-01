package com.qualaroo.internal.network;

import androidx.annotation.RestrictTo;

import okhttp3.Response;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class ResponseHelper {

    public static boolean shouldRetry(Response response) {
        if (response.isSuccessful()) {
            return false;
        }
        int httpCode = response.code();
        if (httpCode >= 500) {
            return true;
        }
        return httpCode < 400;
    }

    private ResponseHelper() {
        //no instances
    }

}
