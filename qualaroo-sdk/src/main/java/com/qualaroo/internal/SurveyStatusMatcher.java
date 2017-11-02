package com.qualaroo.internal;

import com.qualaroo.QualarooLogger;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.model.SurveyStatus;
import com.qualaroo.internal.storage.LocalStorage;

public class SurveyStatusMatcher extends SurveySpecMatcher {

    private final LocalStorage localStorage;
    private final TimeMatcher timeMatcher;

    public SurveyStatusMatcher(LocalStorage localStorage, TimeMatcher timeMatcher) {
        this.localStorage = localStorage;
        this.timeMatcher = timeMatcher;
    }

    @Override boolean matches(Survey survey) {
        SurveyStatus status = localStorage.getSurveyStatus(survey);
        if (status.hasBeenFinished() && !survey.spec().requireMap().isPersistent()) {
            QualarooLogger.debug("Survey %1$s has already been finished.", survey.canonicalName());
            return false;
        }

        if (survey.spec().requireMap().isOneShot() && status.hasBeenSeen()) {
            QualarooLogger.debug("Survey %1$s has already been seen", survey.canonicalName());
            return false;
        }

        if (!timeMatcher.enoughTimePassedFrom(status.seenAtInMillis())) {
            QualarooLogger.debug("Survey %1$s cannot be shown yet.", survey.canonicalName());
            return false;
        }

        return true;
    }
}
