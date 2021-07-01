package com.qualaroo.internal.network;

public interface Callback<T> {
    void onSuccess(T result);
    void onFailure(Exception exception);
}
