package com.qualaroo.internal;

import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class TimeMatcher {

    private final long pauseBetweenSurveysInMillis;

    @VisibleForTesting TimeProvider timeProvider = new TimeProvider();

    public TimeMatcher(long pauseBetweenSurveysInMillis) {
        this.pauseBetweenSurveysInMillis = pauseBetweenSurveysInMillis;
    }

    public boolean enoughTimePassedFrom(long fromInMillis) {
        return timeProvider.getCurrentTimeMillis() - fromInMillis >= pauseBetweenSurveysInMillis;
    }

    class TimeProvider {
        long getCurrentTimeMillis() {
            return System.currentTimeMillis();
        }
    }
}
