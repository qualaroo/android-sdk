package com.qualaroo.util;

import androidx.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface TimeProvider {
    long currentTimeMillis();
}
