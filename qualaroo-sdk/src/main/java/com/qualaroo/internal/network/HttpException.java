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
public class HttpException extends RuntimeException {

    private final int httpCode;

    HttpException(int httpCode) {
        this.httpCode = httpCode;
    }

    public int httpCode() {
        return httpCode;
    }
}
