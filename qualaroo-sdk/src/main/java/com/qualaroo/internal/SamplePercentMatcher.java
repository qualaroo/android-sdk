/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.internal;

import com.qualaroo.internal.model.Survey;

public class SamplePercentMatcher extends SurveySpecMatcher {

    private final UserGroupPercentageProvider percentageProvider;

    public SamplePercentMatcher(UserGroupPercentageProvider percentageProvider) {
        this.percentageProvider = percentageProvider;
    }

    @Override boolean matches(Survey survey) {
        Integer samplePercent = survey.spec().requireMap().samplePercent();
        if (samplePercent == null) {
            return true;
        }
        int percent = percentageProvider.userGroupPercent(survey);
        return percent < samplePercent;
    }
}
