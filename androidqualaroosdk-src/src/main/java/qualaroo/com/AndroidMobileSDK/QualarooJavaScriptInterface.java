package qualaroo.com.AndroidMobileSDK;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;

import qualaroo.com.AndroidMobileSDK.Api.QMRequest;

/**
 * Created by Artem Orynko on 31.09.16.
 * Copyright © 2016 Qualaroo. All rights reserved.
 */

class QualarooJavaScriptInterface {

    private QualarooController mQualarooController;
    Context mContext;

    // Tag for debug
    private static final String TAG = QualarooJavaScriptInterface.class.getSimpleName();


    QualarooJavaScriptInterface(Context context, QualarooController qualarooController) {
        mContext = context;
        mQualarooController = qualarooController;
    }

    @JavascriptInterface
    public void surveyScreenerReady() {
        Log.d(TAG, "Screener is showing.");
        mQualarooController.performShowSurveyAnimation();
    }

    @JavascriptInterface
    public void surveyShow() {
        Log.d(TAG, "Survey is showing.");
        mQualarooController.performShowSurveyAnimation();
    }

    @JavascriptInterface
    public void surveyClosed() {
        Log.d(TAG, "Survey closed.");

        if (mQualarooController.mWebView.getVisibility() == View.VISIBLE) {
            Log.d(TAG, "Hiding survey.");
            mQualarooController.performHideSurveyAnimation();
        }
    }

    @JavascriptInterface
    public void surveyCloseButtonTapped(boolean isMinimized) {
        mQualarooController.mMinimized = isMinimized;
        Log.d(TAG, "Close/minimize/maximize tapped " + isMinimized);

        if (isMinimized) {
            mQualarooController.setSuggestedHeight(48);
            mQualarooController.updateHeight();
        } else {
            mQualarooController.updateHeight();
        }
    }

    @JavascriptInterface
    public void surveyHeightChanged(float suggestedHeight) {

        boolean minimized = mQualarooController.mMinimized;

        if (!minimized) {
            Log.d(TAG, "Survey height changed. New height suggested: " + suggestedHeight);

            mQualarooController.setSuggestedHeight(suggestedHeight);
            mQualarooController.updateHeight();
        }
    }

    @JavascriptInterface
    public void globalUnhandledJSError(String error) {
        Log.d(TAG, "Unhandled global JS Error: " + error);
    }

    @JavascriptInterface

    public void surveyUndeliveredAnswerRequest(String request) {
        Log.d(TAG, "Undelivered answer request: " + request);

        if (mQualarooController != null && !request.isEmpty()) {
            mQualarooController.mDelegate.errorSendingReportRequest(request);
        }
    }

    @JavascriptInterface
    public void qualarooScriptLoadSuccess(String message) {
        Log.d(TAG, "Load script: " + message);
        mQualarooController.mQualarooScriptLoaded = true;
        mQualarooController.setupIdentityCode();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            String jsString = "isOldVersion = true;";
            mQualarooController.evaluateJavaScript(jsString, null);
        }
    }

    @JavascriptInterface
    public void qualarooScriptLoadError(String message) {
        Log.d(TAG, "Failed to load script: " + message);

        mQualarooController.mQualarooScriptLoaded = false;

        if (mQualarooController != null && !message.isEmpty()) {
            mQualarooController.mDelegate.errorLoadingQualarooScript(message);
        }

    }

    @JavascriptInterface
    public void qualarooStartScroll() {
        Log.d(TAG, "Srart scroll down");
        //disable touch in LinearLayout and WebView while demoScroll
        mQualarooController.setInteraction(mQualarooController.mLinearLayout, true);
        mQualarooController.setInteraction(mQualarooController.mWebView, true);
    }

    @JavascriptInterface
    public void qualarooStopScroll(String alias) {
        Log.d(TAG, "Stop scroll up");
        //enable touch in LinearLayout and WebView after demoScroll
        mQualarooController.setInteraction(mQualarooController.mLinearLayout, false);
        mQualarooController.setInteraction(mQualarooController.mWebView, false);
        if (!mQualarooController.mSecretKey.equals("")) {
            mQualarooController.addInfoAboutSurveyAlias(alias);
        }
    }

    @JavascriptInterface
    public void getSurveysInfo(final String survey) {
        if (!mQualarooController.mSecretKey.equals("")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    QMRequest request = QMRequest.getInstance();
                    mQualarooController.mSurveys = request.getSurveyInfo(survey);
                    mQualarooController.mSurveysLoaded = true;
                }
            }).start();
        } else {
            mQualarooController.mSurveysLoaded = true;
        }
    }
}
