package com.qualaroo.internal.storage;

import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.model.SurveyStatus;

import java.util.List;

public interface LocalStorage {
    void storeFailedReportRequest(String reportRequestUrl);
    void removeReportRequest(String reportRequestUrl);
    List<String> getFailedReportRequests(int numOfRequests);
    void markSurveyAsSeen(Survey survey);
    void markSurveyFinished(Survey survey);
    SurveyStatus getSurveyStatus(Survey survey);
}
