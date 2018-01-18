package com.qualaroo;

/**
 * {@link SurveyOptions} class represent's a set of different behaviour modifiers when displaying a survey.
 */
public class SurveyOptions {

    static SurveyOptions defaultOptions() {
        return new SurveyOptions(false);
    }

    private final boolean ignoreTargeting;

    private SurveyOptions(boolean ignoreTargeting) {
        this.ignoreTargeting = ignoreTargeting;
    }

    boolean ignoreTargeting() {
        return ignoreTargeting;
    }

    public static class Builder {
        private boolean ignoreTargeting = false;

        /**
         * Ignores checks for all of targeting options configured in Qualaroo Dashboard.
         * The survey will always be displayed. <b>Use with care!</b>
         * @param ignore true if should ignore targeting options, false otherwise
         * @return {@link SurveyOptions.Builder} instance
         */
        public Builder ignoreSurveysTargeting(boolean ignore) {
            this.ignoreTargeting = ignore;
            return this;
        }

        public SurveyOptions build() {
            return new SurveyOptions(ignoreTargeting);
        }
    }



}
