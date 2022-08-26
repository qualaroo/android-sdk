package com.qualaroo;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.qualaroo.QualarooBase;
import com.qualaroo.QualarooLogger;
import com.qualaroo.internal.ImageProvider;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.network.ResponseHelper;
import com.qualaroo.internal.network.RestClient;
import com.qualaroo.internal.network.SurveysRepository;
import com.qualaroo.internal.storage.LocalStorage;

import java.io.IOException;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.Response;

public class QualarooWorker extends Worker {
    private static final int MAX_UPLOADS_PER_JOB = 50;
    public QualarooWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        QualarooBase qualaroo;
        try {
            qualaroo = getQualarooInstance();
            prefetchSurveyWithImages(qualaroo);
            uploadFailedRequests(qualaroo);
        } catch (QualarooSdkNotProperlyInitializedException e) {
            QualarooLogger.error("Qualaroo instance is not available to Qularoo's Service");

        }
        return Result.success();
    }


    private void prefetchSurveyWithImages(QualarooBase qualarooBase) {
        ImageProvider imageProvider = qualarooBase.imageProvider();
        SurveysRepository surveysRepository = qualarooBase.surveysRepository();
        List<Survey> surveys = surveysRepository.getSurveys();
        for (Survey survey : surveys) {
            imageProvider.getImage(survey.spec().optionMap().logoUrl(), null);
        }
    }

    private void uploadFailedRequests(QualarooBase qualarooBase) {
        final RestClient restClient = qualarooBase.restClient();
        final LocalStorage localStorage = qualarooBase.localStorage();
        final List<String> failedReportRequests = localStorage.getFailedReportRequests(MAX_UPLOADS_PER_JOB);

        if (failedReportRequests.size() == 0) {
            QualarooLogger.debug("No failed reports found");
            return;
        }

        QualarooLogger.info("Attempting to upload %d reports", failedReportRequests.size());
        for (String failedReportRequest : failedReportRequests) {
            try {
                Response response = restClient.get(HttpUrl.parse(failedReportRequest));
                boolean shouldRetry = ResponseHelper.shouldRetry(response);
                if (!shouldRetry) {
                    localStorage.removeReportRequest(failedReportRequest);
                }
            } catch (IOException e) {
                //ignore
            }
        }
    }
    @VisibleForTesting
    @NonNull QualarooBase getQualarooInstance() throws QualarooWorker.QualarooSdkNotProperlyInitializedException {
        QualarooSdk instance = Qualaroo.getInstance();
        if (instance instanceof QualarooBase) {
            return (QualarooBase) instance;
        }
        throw new QualarooWorker.QualarooSdkNotProperlyInitializedException();
    }

    private static class QualarooSdkNotProperlyInitializedException extends Exception {}

}
