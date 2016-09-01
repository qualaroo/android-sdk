package qualaroo.com.androidqualaroosdk.src;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Artem Orynko on 31.09.16.
 * Copyright © 2016 Qualaroo. All rights reserved.
 */

class QualarooJavaScriptInterface {

    private QualarooSurvey      mQualarooSurvey = new QualarooSurvey();
    private QualarooController  mQualarooController = new QualarooController();

    // Tag for debug
    private static final String TAG = QualarooJavaScriptInterface.class.getSimpleName();

    Context mContext;

    QualarooJavaScriptInterface(Context context) {
        mContext = context;
    }

    @JavascriptInterface
    public void surveyScreenerReady() {
        Log.d(TAG, "Screener is showing.");
        mQualarooController.mLinearLayout.setVisibility(View.VISIBLE);
    }

    @JavascriptInterface
    public void qualarooScriptLoadSuccess(String message) {
        Log.d(TAG, "Load script: " + message);
//        mQualarooController.setupIdentityCode();
    }

    @JavascriptInterface
    public void surveyShow() {
        Log.d(TAG, "Survey is showing.");
        mQualarooController.mLinearLayout.setVisibility(View.VISIBLE);
    }

    @JavascriptInterface
    public void surveyClosed() {
        Log.d(TAG, "Survey closed.");

//        if (mQualarooController.mLinearLayout.getVisibility() == View.VISIBLE) {
//            //TODO: animation for hide
//        }
    }

    @JavascriptInterface
    public void surveyCloseButtonTapped(boolean isMinimized) {
        mQualarooController.mMinimized = isMinimized;
        Log.d(TAG, "Close/minimize/maximize tapped " + isMinimized);

        if (isMinimized) {
            //TODO: setup constraint
        } else {
            //TODO: update height
        }
    }

    @JavascriptInterface
    public void surveyHeightChanged(float suggestedHeight) {

        if (!mQualarooController.mMinimized) {
            Log.d(TAG, "Survey height changed. New height suggested: " + suggestedHeight);
            mQualarooController.mSuggestedHeight = suggestedHeight;

            //TODO: update height
        }
    }

    @JavascriptInterface
    public void globalUnhandledJSError(String error) {
        Log.d(TAG, "Unhandled global JS Error: " + error);
    }
    @JavascriptInterface

    public void surveyUndeliveredAnswerRequest(String request) {
        Log.d(TAG, "Undelivered answer request: " + request);

        URL url = null;
        try {
            url = new URL(request);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (mQualarooController != null && url != null) {
//            mQualarooSurvey.surveyActivityErrorSendingReportRequest(url);
        }
    }

    @JavascriptInterface
    public void qualarooScriptLoadError(String message) {
        Log.d(TAG, "Failed to load script: " + message);

        URL url = null;
        try {
            url = new URL(message);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (mQualarooSurvey != null && url != null) {
//            mQualarooSurvey.surveyActivityErrorLoadingQualarooScript(url);
        }
    }
}
