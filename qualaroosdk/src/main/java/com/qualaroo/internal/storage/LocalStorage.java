package com.qualaroo.internal.storage;

import com.qualaroo.internal.model.Survey;

import java.util.List;

public interface LocalStorage {

    void storeFailedReportRequest(String reportRequestUrl);
    void removeReportRequest(String reportRequestUrl);
    List<String> getFailedReportRequests(int numOfRequests);
    void markSurveyFinished(Survey survey);
    boolean isSurveyFinished(Survey survey);

}
