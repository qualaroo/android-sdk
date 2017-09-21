package com.qualaroo.internal.network;

import com.qualaroo.internal.SessionInfo;
import com.qualaroo.internal.UserInfo;
import com.qualaroo.internal.model.Survey;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import okhttp3.HttpUrl;

public class SurveyClient {

    private final RestClient restClient;
    private final ApiConfig apiConfig;
    private final UserInfo userInfo;
    private final SessionInfo sessionInfo;
    private final Executor executor;

    public SurveyClient(RestClient restClient, ApiConfig apiConfig, SessionInfo sessionInfo, UserInfo userInfo, Executor executor) {
        this.restClient = restClient;
        this.apiConfig = apiConfig;
        this.sessionInfo = sessionInfo;
        this.userInfo = userInfo;
        this.executor = executor;
    }

    public void fetchSurveys(final Callback<List<Survey>> callback) {
        HttpUrl.Builder builder = apiConfig.qualarooApi().newBuilder()
                .addPathSegment("surveys")
                .addQueryParameter("site_id", "64832")
                .addQueryParameter("spec", "1")
                .addQueryParameter("no_superpack", "1");
        injectAnalyticsParams(builder);
        final HttpUrl requestUrl = builder.build();
        executor.execute(new Runnable() {
            @Override public void run() {
                Result<Survey[]> result = restClient.get(requestUrl, Survey[].class);
                if (result.isSuccessful()) {
                    callback.onSuccess(Arrays.asList(result.getData()));
                } else {
                    callback.onFailure(result.getException());
                }
            }
        });
    }

    private void injectAnalyticsParams(HttpUrl.Builder httpUrlBuilder) {
        httpUrlBuilder
                .addQueryParameter("SDK_version", sessionInfo.sdkVersion())
                .addQueryParameter("client_app", sessionInfo.appName())
                .addEncodedQueryParameter("device_type", sessionInfo.deviceType())
                .addQueryParameter("android_version", sessionInfo.androidVersion())
                .addQueryParameter("device_ID", userInfo.getDeviceId());
    }
}
