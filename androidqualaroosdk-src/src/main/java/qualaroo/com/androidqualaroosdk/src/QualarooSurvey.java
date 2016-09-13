package qualaroo.com.androidqualaroosdk.src;

import android.app.Activity;
import android.util.Log;

/**
 * Created by Artem Orynko on 23.08.16.
 * Copyright © 2016 Qualaroo. All rights reserved.
 */

public class QualarooSurvey {

    // Tag for debug
    private static final String TAG = QualarooSurvey.class.getSimpleName();

    private QualarooController mQualarooController;
    private String mAPIKey;

    private Activity mHostActivity;

    //region Public Methods

    public QualarooSurvey(Activity hostActivity) {
        mHostActivity = hostActivity;
    }

    public QualarooSurvey initWithAPIKey(String APIKey) {

        mQualarooController = new QualarooController(mHostActivity, this).initWithAPIKey(APIKey);

        if (mQualarooController == null) {
            return null;
        }

        mAPIKey = APIKey;

        //TODO: get Network connection

        return this;
    }

    public boolean attachToActivity() {
        return attachToActivity(QualarooSurveyPosition.QUALAROO_SURVEY_POSITION_BOTTOM);

    }
    public boolean attachToActivity(QualarooSurveyPosition position) {
        return mQualarooController.attachToActivity(position);
    }

    public boolean removeFromActivity() {

        if (mQualarooController == null) {
            return true;
        }
        boolean success = mQualarooController.removeFromActivity();

        mQualarooController = null;

        return success;
    }

    public void showSurvey(String surveyAlias) {

        mQualarooController.showSurvey(surveyAlias, false);
    }

    public void showSurvey(String surveyAlias, boolean shouldForce) {

        mQualarooController.showSurvey(surveyAlias, shouldForce);
    }

    //endregion

    //region Protected Methods


    protected void errorSendingReportRequest(String reportRequestURL) {
        Log.d(TAG, "Unable to send unfulfilled request with URL: " + reportRequestURL);

    }

    protected void errorLoadingQualarooScript(String scriptURL) {
        Log.d(TAG, "Unable to load Qualaroo script at URL: " + scriptURL);
    }

    //endregion
}
