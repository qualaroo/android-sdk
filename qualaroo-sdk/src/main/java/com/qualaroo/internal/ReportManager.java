package com.qualaroo.internal;

import androidx.annotation.RestrictTo;

import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.model.UserResponse;
import com.qualaroo.internal.network.ReportClient;

import java.util.List;
import java.util.concurrent.Executor;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class ReportManager {

    private final ReportClient reportClient;
    private final Executor executor;

    public ReportManager(ReportClient reportClient, Executor executor) {
        this.reportClient = reportClient;
        this.executor = executor;
    }

    public void reportImpression(final Survey survey) {
        executor.execute(new Runnable() {
            @Override public void run() {
                reportClient.reportImpression(survey);
            }
        });
    }

    public void reportUserResponse(final Survey survey, final UserResponse userResponse) {
        executor.execute(new Runnable() {
            @Override public void run() {
                reportClient.reportUserResponse(survey, userResponse);
            }
        });
    }

    public void reportUserResponse(final Survey survey, final List<UserResponse> userResponse) {
        executor.execute(new Runnable() {
            @Override public void run() {
                reportClient.reportUserResponse(survey, userResponse);
            }
        });
    }
}
