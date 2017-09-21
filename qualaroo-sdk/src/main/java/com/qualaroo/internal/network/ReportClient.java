package com.qualaroo.internal.network;

import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.Survey;

import java.util.List;
import java.util.Locale;

import okhttp3.HttpUrl;

public class ReportClient {
    private final RestClient restClient;
    private final ApiConfig apiConfig;

    public ReportClient(RestClient restClient, ApiConfig apiConfig) {
        this.restClient = restClient;
        this.apiConfig = apiConfig;
    }

    public Result<String> recordImpression(Survey survey) {
        final HttpUrl requestUrl = apiConfig.reportApi().newBuilder()
                .addPathSegment("c.js")
                .addQueryParameter("id", String.valueOf(survey.id()))
                .build();
        return restClient.get(requestUrl);
    }

    public Result<String> recordAnswer(Survey survey, Question question, List<Answer> answerList) {
        HttpUrl.Builder builder = apiConfig.reportApi().newBuilder()
                .addPathSegment("r.js")
                .addQueryParameter("id", String.valueOf(survey.id()));
        injectAnswers(builder, question, answerList);
        final HttpUrl url = builder.build();
        return restClient.get(url);
    }

    public Result<String> recordTextAnswer(Survey survey, Question question, String answer) {
        HttpUrl.Builder builder = apiConfig.reportApi().newBuilder()
                .addPathSegment("r.js")
                .addQueryParameter("id", String.valueOf(survey.id()));
        builder.addEncodedQueryParameter(String.format(Locale.ROOT,"r[%d][text]", question.id()), answer);
        final HttpUrl url = builder.build();
        return restClient.get(url);
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
