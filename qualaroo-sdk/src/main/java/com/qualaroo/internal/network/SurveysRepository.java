package com.qualaroo.internal.network;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.qualaroo.QualarooLogger;
import com.qualaroo.internal.SessionInfo;
import com.qualaroo.internal.UserInfo;
import com.qualaroo.internal.model.Survey;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.HttpUrl;

public class SurveysRepository {

    private final String siteId;
    private final RestClient restClient;
    private final ApiConfig apiConfig;
    private final UserInfo userInfo;
    private final SessionInfo sessionInfo;
    private final long staleDataTimeLimitInMillis;

    private final Object lock = new Object();

    private CachedResult cachedResult = new CachedResult(null, 0);

    public SurveysRepository(String siteId, RestClient restClient, ApiConfig apiConfig, SessionInfo sessionInfo, UserInfo userInfo, long staleDataTimeLimitInMillis) {
        this.siteId = siteId;
        this.restClient = restClient;
        this.apiConfig = apiConfig;
        this.sessionInfo = sessionInfo;
        this.userInfo = userInfo;
        this.staleDataTimeLimitInMillis = staleDataTimeLimitInMillis;
    }

    public @NonNull List<Survey> getSurveys() {
        synchronized (lock) {
            if (isInvalid(cachedResult)) {
                refreshData();
            } else if (isStale(cachedResult)) {
                refreshDataAsync();
            }
            return returnSafely(cachedResult.surveys);
        }
    }

    private void refreshData() {
        List<Survey> surveys = fetchSurveys();
        if (surveys != null) {
            synchronized (lock) {
                cachedResult = new CachedResult(surveys, System.currentTimeMillis());
            }
        }
    }

    private void refreshDataAsync() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
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
            QualarooLogger.debug("Acquired %1$d surveys", result.getData().length);
            return Arrays.asList(result.getData());
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
                .addQueryParameter("SDK_version", sessionInfo.sdkVersion())
                .addQueryParameter("client_app", sessionInfo.appName())
                .addEncodedQueryParameter("device_type", sessionInfo.deviceType())
                .addQueryParameter("android_version", sessionInfo.androidVersion())
                .addQueryParameter("device_ID", userInfo.getDeviceId());
    }

    private boolean isStale(CachedResult cachedResult) {
        return System.currentTimeMillis() - cachedResult.timestamp > staleDataTimeLimitInMillis;
    }

    private boolean isInvalid(CachedResult cachedResult) {
        return cachedResult.surveys == null;
    }

    private static final class CachedResult {
        private final List<Survey> surveys;
        private final long timestamp;

        CachedResult(List<Survey> surveys, long timestamp) {
            this.surveys = surveys;
            this.timestamp = timestamp;
        }
    }


}
