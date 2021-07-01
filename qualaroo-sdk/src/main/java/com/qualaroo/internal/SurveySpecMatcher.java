package com.qualaroo.internal;

import com.qualaroo.internal.model.Survey;

abstract class SurveySpecMatcher {
    abstract boolean matches(Survey survey);
}
