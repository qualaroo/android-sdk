package com.qualaroo.internal;

import android.support.annotation.RestrictTo;

import com.qualaroo.QualarooLogger;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.model.SurveyStatus;
import com.qualaroo.internal.storage.LocalStorage;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class SurveyDisplayQualifier {

    private final LocalStorage localStorage;
    private final UserPropertiesMatcher propertiesMatcher;
    private final TimeMatcher timeMatcher;
    private final UserIdentityMatcher userIdentityMatcher;
    private final DeviceTypeMatcher deviceTypeMatcher;

    public SurveyDisplayQualifier(LocalStorage localStorage, UserPropertiesMatcher propertiesMatcher, TimeMatcher timeMatcher, UserIdentityMatcher userIdentityMatcher, DeviceTypeMatcher deviceTypeMatcher) {
        this.localStorage = localStorage;
        this.propertiesMatcher = propertiesMatcher;
        this.timeMatcher = timeMatcher;
        this.userIdentityMatcher = userIdentityMatcher;
        this.deviceTypeMatcher = deviceTypeMatcher;
    }

    public boolean shouldShowSurvey(Survey survey) {
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
            QualarooLogger.debug("Survey %1$s has already been seen", survey.canonicalName());
            return false;
        }

        if (!propertiesMatcher.match(survey.spec().requireMap().customMap())) {
            QualarooLogger.debug("User properties do not match survey %1$s's requirements", survey.canonicalName());
            return false;
        }

        if (!userIdentityMatcher.shouldShow(survey)) {
            QualarooLogger.debug("User identity type does not match survey %1$s`s requirements", survey.canonicalName());
            return false;
        }

        if (!deviceTypeMatcher.doesDeviceMatch(survey)) {
            QualarooLogger.debug("User device type does not match survey %1$s`s requirements", survey.canonicalName());
            return false;
        }

        return true;
    }
}
