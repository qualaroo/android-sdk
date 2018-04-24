package com.qualaroo.util;

import android.support.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface TimeProvider {
    long currentTimeMillis();
}
