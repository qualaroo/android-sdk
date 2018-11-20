/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.internal.network;

import android.support.annotation.RestrictTo;

import com.qualaroo.util.TimeProvider;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class NonWorkingCache<T> extends Cache<T> {
    public NonWorkingCache() {
        super(new TimeProvider() {
            @Override public long currentTimeMillis() {
                return System.currentTimeMillis();
            }
        }, 0);
    }

    @Override public boolean isStale() {
        return true;
    }

    @Override public boolean isInvalid() {
        return true;
    }
}
