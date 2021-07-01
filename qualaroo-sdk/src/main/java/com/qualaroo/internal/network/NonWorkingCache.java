package com.qualaroo.internal.network;

import androidx.annotation.RestrictTo;

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
