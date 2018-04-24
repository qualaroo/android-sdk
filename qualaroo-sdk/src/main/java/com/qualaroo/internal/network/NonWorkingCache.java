package com.qualaroo.internal.network;

import android.support.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class NonWorkingCache<T> extends Cache<T> {
    public NonWorkingCache() {
        super(System::currentTimeMillis, 0);
    }

    @Override public boolean isStale() {
        return true;
    }

    @Override public boolean isInvalid() {
        return true;
    }
}
