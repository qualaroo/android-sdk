package com.qualaroo.MobileSDK;

import android.app.Activity;
import android.content.Context;

import com.qualaroo.MobileSDK.sdk.QualarooSurveyController;

/**
 * Created by Artem Orynko on 13.02.17.
 * Copyright © 2016 Qualaroo. All rights reserved.
 */

public class QualarooMobile {

    private Context context;
    private QualarooSurveyController surveyController;

    /**
     * Instantiates a new QualarooMobile with Context
     * @param context Application context
     */
    public QualarooMobile(Context context) {
        this.context = context;
    }

    /**
     * Get the current state of QualarooMobile
     * @return QMState
     */
    public QMState getState() {
        return surveyController.getState();
    }

    /**
     * Get the report for the state of QualarooMobile
     * @return QMReport
     */
    public QMReport getStateReport() {
        return surveyController.getReport();
    }

    /**
     * Initialize QualarooMobile with APIKey and API Secret key
     * @param apiKey APIKey
     * @param secretKey API Secret key
     * @return QualarooMobile
     * @throws Exception In case initialization fails, an error will be returned.
     */
    public QualarooMobile init(String apiKey, String secretKey) throws Exception {

        this.surveyController = new QualarooSurveyController(this.context).init(apiKey, secretKey);

        return this;
    }

    /**
     * Initialize QualarooMobile with APIKey
     * @param apiKey APIKey
     * @return QualarooMobile
     * @throws Exception In case initialization fails, an error will be returned.
     */
    public QualarooMobile init(String apiKey) throws Exception {
        return init(apiKey, "");
    }

    /**
     * Attaches QualarooMobile to a given Activity
     * @param activity The Activity that will be used to host survey
     * @param position The attachment position (see 'QMPosition' for supported position)
     * @param callback If QualarooMobile has any warning or error you can check it inside this callback
     * @return true if attach or false if not attached
     */
    public boolean attachToActivity(Activity activity, QMPosition position, QMCallback callback) {
        return surveyController.attachToActivity(activity, position, callback);
    }

    /**
     * Attaches QualarooMobile to a given Activity
     * @param activity The Activity that will be used to host survey
     * @param callback If QualarooMobile has any warning or error you can check it inside this callback
     * @return true if attach or false if not attached
     */
    public boolean attachToActivity(Activity activity, QMCallback callback) {
        return surveyController.attachToActivity(activity, QMPosition.BOTTOM, callback);
    }

    /**
     * Removes a previously attached survey from Activity
     * @return true if remove or false if not removed
     */
    public boolean removeFromActivity() {
        return surveyController.removeFromActivity();
    }

    /**
     * Displays a survey with a given alias inside a Activity
     * @param alias The survey's alias to display
     * @param shouldForce Force a survey to show ovverriding target settings
     * @param callback If QualarooMobile has any warning or error you can check it inside this callback
     */
    public void showSurvey(String alias, boolean shouldForce, QMCallback callback) {
        surveyController.showSurvey(alias, shouldForce, callback);
    }

    /**
     * Displays a survey with a given alias inside a Activity
     * @param alias The survey's alias to display
     * @param callback If QualarooMobile has any warning or error you can check it inside this callback
     */
    public void showSurvey(String alias, QMCallback callback) {
        showSurvey(alias, false, callback);
    }

    /**
     * Destroys QualarooMobile
     */
    public void close() {
        surveyController.close();
        surveyController = null;
    }

    /**
     * Setup blur effect for background
     * @param style Background color (see 'QMBackgroundColor' for supported color)
     * @param alpha Set opacity for background (from 0 to 255)
     * @return true if set or false if not set
     */
    public boolean setBackgroundStyle(QMBackgroundColor style, int alpha) {
        return surveyController.setBlurViewBackgroundColor(style, alpha);
    }

    /**
     * Set the custom Identity Code
     * @param identity Identity Code based on the string
     */
    public void setIdentityCode(String identity) {
        surveyController.setIdentityCode(identity);
    }
}
