package com.qualaroo.internal.network;

public class HttpException extends RuntimeException {

    private final int httpCode;

    HttpException(int httpCode) {
        this.httpCode = httpCode;
    }

    public int httpCode() {
        return httpCode;
    }
}
