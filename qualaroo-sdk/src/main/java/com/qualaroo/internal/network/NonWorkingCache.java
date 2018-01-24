package com.qualaroo.internal.network;

import android.support.annotation.RestrictTo;

import com.qualaroo.util.TimeProvider;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class NonWorkingCache<T> extends Cache<T> {
    public NonWorkingCache() {
        super(TimeProvider.DEFAULT, 0);
    }

    @Override public boolean isStale() {
        return true;
    }

    @Override public boolean isInvalid() {
        return true;
    }
}
