package com.qualaroo.MobileSDK.sdk;

import android.os.Build;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;

import com.qualaroo.MobileSDK.QMState;

/**
 * Created by Artem Orynko on 31.09.16.
 * Copyright © 2016 Qualaroo. All rights reserved.
 */

class QMJavaScriptInterface {

    private QualarooSurveyController surveyController;

    private boolean surveyIsShowing = false;

    // Tag for debug
    private static final String TAG = "QualarooMobile";

    QMJavaScriptInterface(QualarooSurveyController surveyController) {
        this.surveyController = surveyController;
    }

    @JavascriptInterface
    public void qualarooScriptLoadSuccess(String message) {
        surveyController.setupIdentityCode();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            String jsString = "isOldVersion = true;";
            surveyController.evaluateJavaScript(jsString, null);
        }
    }
    @JavascriptInterface
    public void globalUnhandledJSError(String error) {
        Log.d(TAG, "Unhandled global JS Error: " + error);
    }

    @JavascriptInterface
    public void getSurveysInfo(final String message) {

        if (surveyController.getState() == QMState.READY) {
            surveyController.setExecuting(true);
            surveyController.setReady(false);

            surveyController.changeState();
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                surveyController.getSurveyInfo(message);
            }
        });
        thread.start();
    }

    @JavascriptInterface
    public void surveyUndeliveredAnswerRequest(String request) {
        Log.d(TAG, "Undelivered answer request: " + request);
    }

    @JavascriptInterface
    public void surveyHeightChanged(float suggestedHeight) {
        Log.d(TAG, "Survey height changed. New height suggested: " + suggestedHeight);

        surveyController.setSuggestedHeight(suggestedHeight);
        surveyController.updateHeight();
    }

    @JavascriptInterface
    public void surveyScreenerReady() {
        Log.d(TAG, "Screener is showing.");
        surveyIsShowing = true;
        surveyController.performShowSurveyAnimation();
    }

    @JavascriptInterface
    public void surveyShow() {
        Log.d(TAG, "Survey is showing.");
        surveyIsShowing = true;
        surveyController.performShowSurveyAnimation();
    }

    @JavascriptInterface
    public void surveyClosed() {
        Log.d(TAG, "Survey closed.");

        if (surveyController.getWebView().getVisibility() == View.VISIBLE) {
            Log.d(TAG, "Hiding survey");
            surveyController.performHideSurveyAnimation();
        }
        surveyIsShowing = false;
    }

    @JavascriptInterface
    public void reloadQualarooData() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (surveyIsShowing) {}
                surveyController.reloadData();
            }
        }).start();
    }

    @JavascriptInterface
    public void qualarooScriptLoadError(String message) {
        Log.d(TAG, "Failed to load script: " + message);

        surveyController.cancel();
    }

    @JavascriptInterface
    public void qualarooStartScroll() {
        Log.d(TAG, "Srart scroll down");
        //disable touch in LinearLayout and WebView while demoScroll
        surveyController.setInteraction(surveyController.getBlurView(), true);
        surveyController.setInteraction(surveyController.getWebView(), true);
    }

    @JavascriptInterface
    public void qualarooStopScroll(String alias) {
        Log.d(TAG, "Stop scroll up");
        //enable touch in LinearLayout and WebView after demoScroll
        surveyController.setInteraction(surveyController.getBlurView(), false);
        surveyController.setInteraction(surveyController.getWebView(), false);
        surveyController.addInfoAboutSurveyAlias(alias);
    }
}
