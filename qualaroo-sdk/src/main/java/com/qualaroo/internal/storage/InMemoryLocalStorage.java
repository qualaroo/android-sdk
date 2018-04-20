package com.qualaroo.internal.storage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.model.SurveyStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class InMemoryLocalStorage implements LocalStorage {

    private final List<String> failedRequests = new ArrayList<>();
    private final Map<Integer, SurveyStatus> surveyStatusMap = new HashMap<>();
    private final Map<String, String> userProperties = new HashMap<>();
    private final Map<Integer, Integer> surveyUserGroupPercents = new HashMap<>();
    private final Map<String, Integer> abTestGroupPercents = new HashMap<>();

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
        builder.setSeenAtInMillis(System.currentTimeMillis());
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
        builder.setSeenAtInMillis(System.currentTimeMillis());
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

    @Override public void storeUserGroupPercent(Survey survey, int percent) {
        surveyUserGroupPercents.put(survey.id(), percent);
    }

    @Override @Nullable public Integer getUserGroupPercent(Survey survey) {
        return surveyUserGroupPercents.get(survey.id());
    }

    @Override public void storeAbTestGroupPercent(List<Survey> surveys, int percent) {
        String key = AbTestStorageKeyHelper.build(surveys);
        abTestGroupPercents.put(key, percent);
    }

    @Override @Nullable public Integer getAbTestGroupPercent(List<Survey> surveys) {
        String key = AbTestStorageKeyHelper.build(surveys);
        return abTestGroupPercents.get(key);
    }

}
