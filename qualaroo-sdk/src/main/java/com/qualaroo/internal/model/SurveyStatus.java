package com.qualaroo.internal.model;

import androidx.annotation.RestrictTo;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class SurveyStatus {

    public static SurveyStatus.Builder builder() {
        return new Builder();
    }

    public static SurveyStatus emptyStatus(Survey survey) {
        return builder()
                .setSurveyId(survey.id())
                .setHasBeenSeen(false)
                .setHasBeenFinished(false)
                .build();
    }

    private int surveyId;
    private boolean hasBeenSeen;
    private boolean hasBeenFinished;
    private long seenAtInMillis;

    private SurveyStatus(int surveyId, boolean hasBeenSeen, boolean hasBeenFinished, long seenAtInMillis) {
        this.surveyId = surveyId;
        this.hasBeenSeen = hasBeenSeen;
        this.hasBeenFinished = hasBeenFinished;
        this.seenAtInMillis = seenAtInMillis;
    }

    public int surveyId() {
        return surveyId;
    }

    public boolean hasBeenSeen() {
        return hasBeenSeen;
    }

    public boolean hasBeenFinished() {
        return hasBeenFinished;
    }

    public long seenAtInMillis() {
        return seenAtInMillis;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SurveyStatus that = (SurveyStatus) o;

        return surveyId == that.surveyId;
    }

    @Override public int hashCode() {
        return surveyId;
    }

    public static class Builder {
        private int surveyId;
        private boolean hasBeenSeen;
        private boolean hasBeenFinished;
        private long seenAtInMillis;

        public Builder setSurveyId(int surveyId) {
            this.surveyId = surveyId;
            return this;
        }

        public Builder setHasBeenSeen(boolean hasBeenSeen) {
            this.hasBeenSeen = hasBeenSeen;
            return this;
        }

        public Builder setHasBeenFinished(boolean hasBeenFinished) {
            this.hasBeenFinished = hasBeenFinished;
            return this;
        }

        public Builder setSeenAtInMillis(long seenAtInMillis) {
            this.seenAtInMillis = seenAtInMillis;
            return this;
        }

        public SurveyStatus build() {
            return new SurveyStatus(surveyId, hasBeenSeen, hasBeenFinished, seenAtInMillis);
        }
    }

}
