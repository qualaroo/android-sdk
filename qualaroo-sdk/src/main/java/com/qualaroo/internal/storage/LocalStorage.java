package com.qualaroo.internal.storage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.model.SurveyStatus;

import java.util.List;
import java.util.Map;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public interface LocalStorage {
    void storeFailedReportRequest(String reportRequestUrl);
    void removeReportRequest(String reportRequestUrl);
    List<String> getFailedReportRequests(int numOfRequests);
    int getFailedRequestsCount();
    void markSurveyAsSeen(Survey survey);
    void markSurveyFinished(Survey survey);
    SurveyStatus getSurveyStatus(Survey survey);
    SurveyStatus isSurveyShowed(int surveyId);
    void updateUserProperty(@NonNull String key, @Nullable String value);
    Map<String, String> getUserProperties();
    void storeUserGroupPercent(Survey survey, int percent);
    /**
     * Returns stored user group percentage for provided survey.
     * @param survey
     * @return stored value - null if there is no value stored for survey
     */
    @Nullable Integer getUserGroupPercent(Survey survey);

    void storeAbTestGroupPercent(List<Survey> surveys, int percent);
    @Nullable Integer getAbTestGroupPercent(List<Survey> surveys);
}
