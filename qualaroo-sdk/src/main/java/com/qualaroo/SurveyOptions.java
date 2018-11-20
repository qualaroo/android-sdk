/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

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
         * While active, the survey will always be displayed. <b>Use with care!</b>
         * @param ignore true if should ignore targeting options, false otherwise
         * @return {@link SurveyOptions.Builder} instance
         */
        public Builder ignoreSurveyTargeting(boolean ignore) {
            this.ignoreTargeting = ignore;
            return this;
        }

        public SurveyOptions build() {
            return new SurveyOptions(ignoreTargeting);
        }
    }

}
