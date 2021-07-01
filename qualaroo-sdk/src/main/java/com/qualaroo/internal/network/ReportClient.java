package com.qualaroo.internal.network;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.qualaroo.internal.SdkSession;
import com.qualaroo.internal.SurveySession;
import com.qualaroo.internal.UserInfo;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.model.UserResponse;
import com.qualaroo.internal.storage.LocalStorage;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Response;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class ReportClient {
    private final RestClient restClient;
    private final ApiConfig apiConfig;
    private final LocalStorage localStorage;
    private final UserInfo userInfo;
    private final SurveySession surveySession;
    private final SdkSession sdkSession;

    public ReportClient(RestClient restClient, ApiConfig apiConfig, LocalStorage localStorage, UserInfo userInfo, SurveySession surveySession, SdkSession sdkSession) {
        this.restClient = restClient;
        this.apiConfig = apiConfig;
        this.localStorage = localStorage;
        this.userInfo = userInfo;
        this.surveySession = surveySession;
        this.sdkSession = sdkSession;
    }

    public void reportImpression(Survey survey) {
        HttpUrl.Builder builder = apiConfig.reportApi().newBuilder()
                .addPathSegment("c.js")
                .addQueryParameter("id", String.valueOf(survey.id()));
        injectSessionParam(builder);
        final HttpUrl url = builder.build();
        report(url);
    }

    public void reportUserResponse(Survey survey, UserResponse userResponse) {
        HttpUrl.Builder builder = reportUserResponseBuilder(survey);
        injectResponse(builder, userResponse);
        final HttpUrl url = builder.build();
        report(url);
    }

    public void reportUserResponse(Survey survey, List<UserResponse> userResponses) {
        HttpUrl.Builder builder = reportUserResponseBuilder(survey);
        for (UserResponse userResponse : userResponses) {
            injectResponse(builder, userResponse);
        }
        HttpUrl url = builder.build();
        report(url);
    }

    private void report(HttpUrl url) {
        try {
            Response response = restClient.get(url);
            storeIfFailed(response);
        } catch (IOException e) {
            storeFailedRequestForLater(url.toString());
        }
    }

    @NonNull private HttpUrl.Builder reportUserResponseBuilder(Survey survey) {
        HttpUrl.Builder builder = apiConfig.reportApi().newBuilder()
                .addPathSegment("r.js")
                .addQueryParameter("id", String.valueOf(survey.id()));
        injectUserParams(builder);
        injectAnalyticsParams(builder);
        injectSessionParam(builder);
        return builder;
    }

    private void injectResponse(HttpUrl.Builder builder, UserResponse response) {
        for (UserResponse.Entry entry : response.entries()) {
            long questionId = response.questionId();
            int type = entry.type();
            if (type == UserResponse.Entry.TYPE_TEXT) {
                builder.addQueryParameter(format("r[%d][text]", response.questionId()), entry.text());
            } else if (type == UserResponse.Entry.TYPE_CHOICE) {
                builder.addQueryParameter(format("r[%d][]", questionId), String.valueOf(entry.answerId()));
            } else if (type == UserResponse.Entry.TYPE_CHOICE_WITH_COMMENT) {
                builder.addQueryParameter(format("re[%1$d][%2$d]", questionId, entry.answerId()), String.valueOf(entry.text()));
            }
        }
    }

    private void storeIfFailed(Response response) {
        if (ResponseHelper.shouldRetry(response)) {
            storeFailedRequestForLater(response.request().url().toString());
        }
    }

    private void storeFailedRequestForLater(String url) {
        localStorage.storeFailedReportRequest(url);
    }

    private void injectAnalyticsParams(HttpUrl.Builder httpUrlBuilder) {
        String userId = userInfo.getUserId();
        if (userId != null) {
            httpUrlBuilder.addQueryParameter("i", userInfo.getUserId());
        }
        httpUrlBuilder.addQueryParameter("au", userInfo.getDeviceId());
    }

    private void injectUserParams(HttpUrl.Builder httpUrlBuilder) {
        Map<String, String> userProperties = userInfo.getUserProperties();
        for (Map.Entry<String, String> userParam : userProperties.entrySet()) {
            httpUrlBuilder.addQueryParameter(format("rp[%s]", userParam.getKey()), userParam.getValue());
        }
    }

    private void injectSessionParam(HttpUrl.Builder httpUrlBuilder) {
        httpUrlBuilder.addQueryParameter("u", surveySession.uuid())
                .addQueryParameter("sdk_version", sdkSession.sdkVersion())
                .addQueryParameter("app_id", sdkSession.appName())
                .addQueryParameter("device_model", sdkSession.deviceModel())
                .addQueryParameter("os_version", sdkSession.androidVersion())
                .addQueryParameter("os", sdkSession.os())
                .addQueryParameter("resolution", sdkSession.resolution())
                .addQueryParameter("device_type", sdkSession.deviceType())
                .addQueryParameter("language", sdkSession.language());
    }

    private static String format(String format, Object... args) {
        return String.format(Locale.ROOT, format, args);
    }
}
