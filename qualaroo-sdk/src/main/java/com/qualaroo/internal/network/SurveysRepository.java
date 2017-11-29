package com.qualaroo.internal.network;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import com.qualaroo.QualarooLogger;
import com.qualaroo.internal.SessionInfo;
import com.qualaroo.internal.UserInfo;
import com.qualaroo.internal.model.Survey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import okhttp3.HttpUrl;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class SurveysRepository {

    private static final String SURVEY_SDK_TYPE = "sdk";

    private final String siteId;
    private final RestClient restClient;
    private final ApiConfig apiConfig;
    private final UserInfo userInfo;
    private final SessionInfo sessionInfo;
    private final Cache<List<Survey>> cache;

    private final Object lock = new Object();

    public SurveysRepository(String siteId, RestClient restClient, ApiConfig apiConfig, SessionInfo sessionInfo, UserInfo userInfo, Cache<List<Survey>> cache) {
        this.siteId = siteId;
        this.restClient = restClient;
        this.apiConfig = apiConfig;
        this.sessionInfo = sessionInfo;
        this.userInfo = userInfo;
        this.cache = cache;
    }

    public @NonNull List<Survey> getSurveys() {
        synchronized (lock) {
            if (cache.isInvalid()) {
                refreshData();
            } else if (cache.isStale()) {
                refreshDataAsync();
            }
            return returnSafely(cache.get());
        }
    }

    private void refreshData() {
        List<Survey> surveys = fetchSurveys();
        if (surveys != null) {
            synchronized (lock) {
                cache.put(surveys);
            }
        }
    }

    private void refreshDataAsync() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override public void run() {
                refreshData();
            }
        });
    }

    private @NonNull List<Survey> returnSafely(@Nullable List<Survey> surveys) {
        if (surveys == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(surveys);
    }

    private @Nullable List<Survey> fetchSurveys() {
        final HttpUrl requestUrl = buildSurveyRequestUrl();
        Result<Survey[]> result = restClient.get(requestUrl, Survey[].class);
        if (result.isSuccessful()) {
            List<Survey> surveys = new ArrayList<>();
            for (Survey survey : result.getData()) {
                if (SURVEY_SDK_TYPE.equals(survey.type()) && survey.isActive()) {
                    surveys.add(survey);
                }
            }
            QualarooLogger.debug("Acquired %1$d surveys", surveys.size());
            return surveys;
        } else {
            QualarooLogger.debug("Could not acquire surveys");
            return null;
        }
    }

    private HttpUrl buildSurveyRequestUrl() {
        HttpUrl.Builder builder = apiConfig.qualarooApi().newBuilder()
                .addPathSegment("surveys")
                .addQueryParameter("site_id", siteId)
                .addQueryParameter("spec", "1")
                .addQueryParameter("no_superpack", "1");
        injectAnalyticsParams(builder);
        return builder.build();
    }

    private void injectAnalyticsParams(HttpUrl.Builder httpUrlBuilder) {
        httpUrlBuilder
                .addQueryParameter("sdk_version", sessionInfo.sdkVersion())
                .addQueryParameter("client_app", sessionInfo.appName())
                .addQueryParameter("device_type", sessionInfo.deviceType())
                .addQueryParameter("os_version", sessionInfo.androidVersion())
                .addQueryParameter("os", "Android")
                .addQueryParameter("device_id", userInfo.getDeviceId());
    }

}
