package com.qualaroo.internal.network;

import android.support.annotation.RestrictTo;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class HttpException extends RuntimeException {

    private final int httpCode;

    HttpException(int httpCode) {
        this.httpCode = httpCode;
    }

    public int httpCode() {
        return httpCode;
    }
}
