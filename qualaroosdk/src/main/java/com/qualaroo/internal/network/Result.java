package com.qualaroo.internal.network;

public class Result<T> {

    private final T data;
    private final Exception exception;

    private Result(T data, Exception exception) {
        this.data = data;
        this.exception = exception;
    }

    public T getData() {
        return data;
    }

    Exception getException() {
        return exception;
    }

    boolean isSuccessful() {
        return exception == null;
    }

    static <T> Result<T> of(T data) {
        return new Result<>(data, null);
    }

    static <T> Result<T> error(Exception e) {
        return new Result<>(null, e);
    }
 }
