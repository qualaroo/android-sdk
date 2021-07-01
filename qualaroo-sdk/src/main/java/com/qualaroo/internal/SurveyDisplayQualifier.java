package com.qualaroo.internal;

import androidx.annotation.RestrictTo;

import com.qualaroo.QualarooLogger;
import com.qualaroo.internal.model.Survey;

import java.util.ArrayList;
import java.util.List;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class SurveyDisplayQualifier {

    public static Builder builder() {
        return new Builder();
    }

    private List<SurveySpecMatcher> surveySpecMatchers = new ArrayList<>();

    private SurveyDisplayQualifier(List<SurveySpecMatcher> matchers) {
        this.surveySpecMatchers.addAll(matchers);
    }

    public boolean doesQualify(Survey survey) {
        for (SurveySpecMatcher surveySpecMatcher : surveySpecMatchers) {
            if (!surveySpecMatcher.matches(survey)) {
                QualarooLogger.debug("User properties do not match survey %1$s's requirements", survey.canonicalName());
                return false;
            }
        }
        return true;
    }

    public static class Builder {
        private List<SurveySpecMatcher> matchers = new ArrayList<>();

        public Builder register(SurveySpecMatcher surveySpecMatcher) {
            matchers.add(surveySpecMatcher);
            return this;
        }

        public SurveyDisplayQualifier build() {
            return new SurveyDisplayQualifier(matchers);
        }
    }
}
