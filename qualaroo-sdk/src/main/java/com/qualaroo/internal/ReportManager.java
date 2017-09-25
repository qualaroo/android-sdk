package com.qualaroo.internal;

import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.network.ReportClient;
import com.qualaroo.internal.network.Result;
import com.qualaroo.internal.storage.LocalStorage;

import java.util.List;
import java.util.concurrent.Executor;

public class ReportManager {

    private final ReportClient reportClient;
    private final LocalStorage localStorage;
    private final Executor executor;

    public ReportManager(ReportClient reportClient, LocalStorage localStorage, Executor executor) {
        this.reportClient = reportClient;
        this.localStorage = localStorage;
        this.executor = executor;
    }

    public void recordImpression(Survey survey) {
        Result<String> result = reportClient.recordImpression(survey);
    }

    public void recordAnswer(Survey survey, Question question, List<Answer> answers) {
        Result<String> result = reportClient.recordAnswer(survey, question, answers);
    }

    public void recordTextAnswer(Survey survey, Question question, String answer) {
        Result<String> result = reportClient.recordTextAnswer(survey, question, answer);

    }

    public static class DummyReportManager extends ReportManager {
        public DummyReportManager() {
            super(null, null, null);
        }

        @Override public void recordImpression(Survey survey) {}

        @Override public void recordAnswer(Survey survey, Question question, List<Answer> answers) {}

        @Override public void recordTextAnswer(Survey survey, Question question, String answer) {}
    }
}
