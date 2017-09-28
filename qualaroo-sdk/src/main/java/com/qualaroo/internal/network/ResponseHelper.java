package com.qualaroo.internal.network;

import okhttp3.Response;

final class ResponseHelper {

    static boolean shouldRetry(Response response) {
        if (response.isSuccessful()) {
            return false;
        }
        int httpCode = response.code();
        if (httpCode >= 500) {
            return true;
        }
        if (httpCode >= 400) {
            return false;
        }
        return true;
    }

    private ResponseHelper() {
        //no instances
    }

}
