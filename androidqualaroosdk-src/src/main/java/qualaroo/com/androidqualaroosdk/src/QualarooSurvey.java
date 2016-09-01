package qualaroo.com.androidqualaroosdk.src;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by Artem Orynko on 23.08.16.
 * Copyright © 2016 Qualaroo. All rights reserved.
 */

public class QualarooSurvey {

    // Tag for debug
    private static final String TAG = QualarooSurvey.class.getSimpleName();

    private QualarooController mQualarooController;
    private String mAPIKey;

    //region Accessors

//    private QualarooController getQualarooSurvey() {
//        if (mQualarooController == null) {
//            mQualarooController = new QualarooController().initWithAPIKey(mAPIKey);
//        }
//        return mQualarooController;
//    }

    //region Public Methods

    public void showSurvey(String surveyAlias, String APIKey, Activity activity) {

        showSurvey(surveyAlias, APIKey, activity, QualarooSurveyPosition.QUALAROO_SURVEY_POSITION_BOTTOM);
    }

    public void showSurvey(String surveyAlias, String APIKey, Activity activity, QualarooSurveyPosition surveyPosition) {

        showSurvey(surveyAlias, APIKey, activity, surveyPosition, false);
    }

    public void showSurvey(String surveyAlias, String APIKey, Activity activity, QualarooSurveyPosition surveyPosition, boolean shouldForce) {

        initWithAPIKey(APIKey, activity);
        mQualarooController.attachToActivity(surveyPosition);
        mQualarooController.showSurvey(surveyAlias, shouldForce);
    }


    //endregion

    //region Private Methods

    private QualarooSurvey initWithAPIKey(String APIKey, Activity activity) {

        mQualarooController = new QualarooController().initWithAPIKey(APIKey, activity);

        if (mQualarooController == null) {
            return null;
        }

        mAPIKey = APIKey;

        //TODO: get Network connection

        return this;
    }

    private void deinit() {
        //TODO: destroy all
    }

    //endregion
}
