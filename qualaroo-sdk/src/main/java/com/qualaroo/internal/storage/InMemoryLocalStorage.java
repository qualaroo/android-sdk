package com.qualaroo.internal.storage;

import com.qualaroo.internal.model.Survey;

import java.util.ArrayList;
import java.util.List;

public class InMemoryLocalStorage implements LocalStorage {

    private final List<String> failedRequests = new ArrayList<>();

    @Override public synchronized void storeFailedReportRequest(String reportRequestUrl) {
        failedRequests.add(reportRequestUrl);
    }

    @Override public synchronized void removeReportRequest(String reportRequestUrl) {
        failedRequests.remove(reportRequestUrl);
    }

    @Override public synchronized List<String> getFailedReportRequests(int numOfRequests) {
        final List<String> result = new ArrayList<>();
        if (failedRequests.size() < numOfRequests) {
            result.addAll(failedRequests);
        } else {
            result.addAll(failedRequests.subList(0, numOfRequests));
        }
        return result;
    }

    @Override public void markSurveyFinished(Survey survey) {

    }

    @Override public boolean isSurveyFinished(Survey survey) {
        return false;
    }
}
