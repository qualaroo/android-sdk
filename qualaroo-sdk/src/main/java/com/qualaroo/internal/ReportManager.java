package com.qualaroo.internal;

import android.support.annotation.RestrictTo;

import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.network.ReportClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class ReportManager {

    private final ReportClient reportClient;
    private final Executor executor;

    public ReportManager(ReportClient reportClient, Executor executor) {
        this.reportClient = reportClient;
        this.executor = executor;
    }

    public void recordImpression(final Survey survey) {
        executor.execute(new Runnable() {
            @Override public void run() {
                reportClient.recordImpression(survey);
            }
        });
    }

    public void recordAnswer(final Survey survey, final Question question, final List<Answer> answers) {
        executor.execute(new Runnable() {
            @Override public void run() {
                reportClient.recordAnswer(survey, question, answers);

            }
        });
    }

    public void recordTextAnswer(final Survey survey, final Question question, final String answer) {
        executor.execute(new Runnable() {
            @Override public void run() {
                reportClient.recordTextAnswer(survey, question, answer);
            }
        });
    }

    public void recordLeadGenAnswer(final Survey survey, final Map<Long, String> questionIdToAnswer) {
        executor.execute(new Runnable() {
            @Override public void run() {
                reportClient.recordLeadGenAnswer(survey, questionIdToAnswer);
            }
        });
    }
}
