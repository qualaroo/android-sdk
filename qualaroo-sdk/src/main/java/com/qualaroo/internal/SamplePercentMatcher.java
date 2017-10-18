package com.qualaroo.internal;

import com.qualaroo.internal.model.Survey;

public class SamplePercentMatcher {

    private final UserGroupPercentageProvider percentageProvider;

    public SamplePercentMatcher(UserGroupPercentageProvider percentageProvider) {
        this.percentageProvider = percentageProvider;
    }

    public boolean matches(Survey survey) {
        Integer samplePercent = survey.spec().requireMap().samplePercent();
        if (samplePercent == null) {
            return true;
        }
        int percent = percentageProvider.userGroupPercent(survey);
        return percent < samplePercent;
    }
}
