package com.qualaroo.internal;

import java.util.concurrent.TimeUnit;

final class TimeMatcher {

    private static final long PAUSE_BETWEEN_SAME_SURVEYS = TimeUnit.DAYS.toMillis(3);

    private final TimeProvider timeProvider;

    public TimeMatcher(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public boolean enoughTimePassedFrom(long fromInMillis) {
        return timeProvider.nowInMillis() - fromInMillis >= PAUSE_BETWEEN_SAME_SURVEYS;
    }
}
