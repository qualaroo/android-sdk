/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.internal;

import android.support.annotation.RestrictTo;

import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.model.UserResponse;
import com.qualaroo.internal.network.ReportClient;

import java.util.List;
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
