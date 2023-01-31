package com.qualaroo.internal.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.qualaroo.QualarooLogger;
import com.qualaroo.internal.SdkSession;
import com.qualaroo.internal.UserInfo;
import com.qualaroo.internal.model.Language;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.QuestionType;
import com.qualaroo.internal.model.Survey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import okhttp3.HttpUrl;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

import android.util.Log;

@RestrictTo(LIBRARY)
public class SurveysRepository {

    private static final String SURVEY_SDK_TYPE = "sdk";

    private final String siteId;
    private final RestClient restClient;
    private final ApiConfig apiConfig;
    private final UserInfo userInfo;
    private final SdkSession sdkSession;
    private final Cache<List<Survey>> cache;

    private final Object lock = new Object();

    public SurveysRepository(String siteId, RestClient restClient, ApiConfig apiConfig, SdkSession sdkSession, UserInfo userInfo, Cache<List<Survey>> cache) {
        this.siteId = siteId;
        this.restClient = restClient;
        this.apiConfig = apiConfig;
        this.sdkSession = sdkSession;
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
            return getCachedSurveys();
        }
    }

    public List<Survey> getCachedSurveys() {
        return returnSafely(cache.get());
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
            List<Survey> surveys = filterOutInvalidSurveys(result.getData());
            QualarooLogger.debug("Acquired %1$d surveys", surveys.size());
//            Log.d("filter_result",surveys.toString());
            return surveys;
        } else {
            QualarooLogger.debug("Could not acquire surveys");
            return null;
        }
    }

    private List<Survey> filterOutInvalidSurveys(Survey[] surveys) {
        List<Survey> result = new ArrayList<>();
        for (Survey survey : surveys) {
            if (!SURVEY_SDK_TYPE.equals(survey.type())) {
                continue;
            }
            if (surveyContainUnknownQuestionTypes(survey)) {
                QualarooLogger.error("Survey [%1$d, %2$s] contains questions unsupported by this version of the SDK!", survey.id(), survey.canonicalName());
                continue;
            }
            result.add(survey);
        }
//        Log.d("filter_result",result.toString());
        return result;
    }

    private boolean surveyContainUnknownQuestionTypes(Survey survey) {
        Map<Language, List<Question>> questionMap = survey.spec().questionList();
        for (Map.Entry<Language, List<Question>> languageListEntry : questionMap.entrySet()) {
            for (Question question : languageListEntry.getValue()) {
                if (question.type() == QuestionType.UNKNOWN) {
                    return true;
                }
            }
        }
        return false;
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
                .addQueryParameter("sdk_version", sdkSession.sdkVersion())
                .addQueryParameter("client_app", sdkSession.appName())
                .addQueryParameter("device_model", sdkSession.deviceModel())
                .addQueryParameter("os_version", sdkSession.androidVersion())
                .addQueryParameter("os", "Android")
                .addQueryParameter("device_id", userInfo.getDeviceId());
    }

}
