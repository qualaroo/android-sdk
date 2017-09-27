package com.qualaroo.internal.network;

import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.storage.LocalStorage;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.HttpUrl;
import okhttp3.Response;

public class ReportClient {
    private final RestClient restClient;
    private final ApiConfig apiConfig;
    private final LocalStorage localStorage;

    public ReportClient(RestClient restClient, ApiConfig apiConfig, LocalStorage localStorage) {
        this.restClient = restClient;
        this.apiConfig = apiConfig;
        this.localStorage = localStorage;
    }

    public void recordImpression(Survey survey) {
        final HttpUrl url = apiConfig.reportApi().newBuilder()
                .addPathSegment("c.js")
                .addQueryParameter("id", String.valueOf(survey.id()))
                .build();
        try {
            Response response = restClient.get(url);
            storeIfFailed(response);
        } catch (IOException e) {
            storeFailedRequestForLater(url.toString());
        }
    }

    public void recordAnswer(Survey survey, Question question, List<Answer> answerList) {
        HttpUrl.Builder builder = apiConfig.reportApi().newBuilder()
                .addPathSegment("r.js")
                .addQueryParameter("id", String.valueOf(survey.id()));
        injectAnswers(builder, question, answerList);
        final HttpUrl url = builder.build();
        try {
            Response response = restClient.get(url);
            storeIfFailed(response);
        } catch (IOException e) {
            storeFailedRequestForLater(url.toString());
        }
    }

    public void recordTextAnswer(Survey survey, Question question, String answer) {
        HttpUrl.Builder builder = apiConfig.reportApi().newBuilder()
                .addPathSegment("r.js")
                .addQueryParameter("id", String.valueOf(survey.id()));
        builder.addEncodedQueryParameter(String.format(Locale.ROOT,"r[%d][text]", question.id()), answer);
        final HttpUrl url = builder.build();
        try {
            Response response = restClient.get(url);
            storeIfFailed(response);
        } catch (IOException e) {
            storeFailedRequestForLater(url.toString());
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

    private void injectAnswers(HttpUrl.Builder builder, Question question, List<Answer> answers) {
        switch (question.type()) {
            case NPS:
                injectNpsAnswer(builder, question, answers.get(0));
                break;
            case RADIO:
                injectRadioAnswer(builder, question, answers.get(0));
                break;
            case CHECKBOX:
                injectCheckboxAnswers(builder, question, answers);
                break;
            default:
                break;
        }
    }

    private void injectNpsAnswer(HttpUrl.Builder builder, Question question, Answer answer) {
        builder.addQueryParameter(String.format(Locale.ROOT,"r[%d]", question.id()), String.valueOf(answer.id()));
    }

    private void injectRadioAnswer(HttpUrl.Builder builder, Question question, Answer answer) {
        builder.addQueryParameter(String.format(Locale.ROOT,"r[%d]", question.id()), String.valueOf(answer.id()));
    }

    private void injectCheckboxAnswers(HttpUrl.Builder builder, Question question, List<Answer> answers) {
        for (Answer answer : answers) {
            builder.addQueryParameter(String.format(Locale.ROOT,"r[%d]", question.id()), String.valueOf(answer.id()));
        }
    }

}
