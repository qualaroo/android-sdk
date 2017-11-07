package com.qualaroo.util;

import android.support.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface TimeProvider {
    long currentTimeMillis();

    TimeProvider DEFAULT = new TimeProvider() {
        @Override public long currentTimeMillis() {
            return System.currentTimeMillis();
        }
    };
}
