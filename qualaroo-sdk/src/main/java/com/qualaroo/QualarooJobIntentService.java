package com.qualaroo;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.JobIntentService;

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

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class QualarooJobIntentService extends JobIntentService {

    static final String ACTION_UPLOAD_REQUESTS = "upload";
    private static final int JOB_ID = 192017;
    private static final int MAX_UPLOADS_PER_JOB = 50;

    static void start(Context context) {
        final Intent uploadIntent = new Intent();
        uploadIntent.setAction(ACTION_UPLOAD_REQUESTS);
        enqueueWork(context, QualarooJobIntentService.class, JOB_ID, uploadIntent);
    }

    @Override protected void onHandleWork(@NonNull Intent intent) {
        if (!ACTION_UPLOAD_REQUESTS.equals(intent.getAction())) {
            QualarooLogger.error("Invalid Qualaroo's Service action.");
            return;
        }
        QualarooBase qualaroo;
        try {
            qualaroo = getQualarooInstance();
        } catch (QualarooSdkNotProperlyInitializedException e) {
            QualarooLogger.error("Qualaroo instance is not available to Qularoo's Service");
            return;
        }
        prefetchSurveyWithImages(qualaroo);
        uploadFailedRequests(qualaroo);
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

    @Override public boolean onStopCurrentWork() {
        return false;
    }
    
    @VisibleForTesting @NonNull QualarooBase getQualarooInstance() throws QualarooSdkNotProperlyInitializedException {
        QualarooSdk instance = Qualaroo.getInstance();
        if (instance instanceof QualarooBase) {
            return (QualarooBase) instance;
        }
        throw new QualarooSdkNotProperlyInitializedException();
    }

    private static class QualarooSdkNotProperlyInitializedException extends Exception {}
}
