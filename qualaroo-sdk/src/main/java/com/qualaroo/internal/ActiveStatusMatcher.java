package com.qualaroo.internal;

import com.qualaroo.internal.model.Survey;

public class ActiveStatusMatcher extends SurveySpecMatcher {

    @Override boolean matches(Survey survey) {
        return survey.isActive();
    }

}
