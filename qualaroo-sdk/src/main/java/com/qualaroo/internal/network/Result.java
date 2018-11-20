/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.internal.network;

import android.support.annotation.RestrictTo;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
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

    public Exception getException() {
        return exception;
    }

    public boolean isSuccessful() {
        return exception == null;
    }

    static <T> Result<T> of(T data) {
        return new Result<>(data, null);
    }

    static <T> Result<T> error(Exception e) {
        return new Result<>(null, e);
    }
 }
