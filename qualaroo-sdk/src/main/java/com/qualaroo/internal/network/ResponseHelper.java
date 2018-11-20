/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.internal.network;

import android.support.annotation.RestrictTo;

import okhttp3.Response;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

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
