package com.qualaroo.internal;

import android.support.annotation.VisibleForTesting;

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
