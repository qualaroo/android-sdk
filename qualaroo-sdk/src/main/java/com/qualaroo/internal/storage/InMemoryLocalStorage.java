package com.qualaroo.internal.storage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.qualaroo.internal.TimeProvider;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.model.SurveyStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryLocalStorage implements LocalStorage {

    private final TimeProvider timeProvider;
    private final List<String> failedRequests = new ArrayList<>();
    private final Map<Integer, SurveyStatus> surveyStatusMap = new HashMap<>();
    private final Map<String, String> userProperties = new HashMap<>();

    public InMemoryLocalStorage(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

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

    @Override public int getFailedRequestsCount() {
        return failedRequests.size();
    }

    @Override public synchronized void markSurveyAsSeen(Survey survey) {
        SurveyStatus.Builder builder = SurveyStatus.builder();
        builder.setSurveyId(survey.id());
        builder.setHasBeenSeen(true);
        builder.setSeenAtInMillis(timeProvider.nowInMillis());
        if (surveyStatusMap.containsKey(survey.id())) {
            SurveyStatus current = surveyStatusMap.get(survey.id());
            builder.setHasBeenFinished(current.hasBeenFinished());
        } else {
            builder.setHasBeenFinished(false);
        }
        surveyStatusMap.put(survey.id(), builder.build());
    }

    @Override public synchronized void markSurveyFinished(Survey survey) {
        SurveyStatus.Builder builder = SurveyStatus.builder();
        builder.setSurveyId(survey.id());
        builder.setHasBeenFinished(true);
        builder.setSeenAtInMillis(timeProvider.nowInMillis());
        if (surveyStatusMap.containsKey(survey.id())) {
            SurveyStatus current = surveyStatusMap.get(survey.id());
            builder.setHasBeenSeen(current.hasBeenSeen());
        } else {
            builder.setHasBeenSeen(false);
        }
        surveyStatusMap.put(survey.id(), builder.build());
    }

    @Override public synchronized SurveyStatus getSurveyStatus(Survey survey) {
        SurveyStatus result = surveyStatusMap.get(survey.id());
        if (result == null) {
            result = SurveyStatus.emptyStatus(survey);
        }
        return result;
    }

    @Override public Map<String, String> getUserProperties() {
        return new HashMap<>(userProperties);
    }

    @Override public void updateUserProperty(@NonNull String key, @Nullable String value) {
        if (value == null) {
            userProperties.remove(key);
        } else {
            userProperties.put(key, value);
        }
    }
}
