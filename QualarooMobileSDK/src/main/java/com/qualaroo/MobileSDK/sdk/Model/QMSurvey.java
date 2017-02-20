package com.qualaroo.MobileSDK.sdk.Model;

import com.qualaroo.MobileSDK.sdk.QMShowSurvey;

import java.util.ArrayList;

/**
 * Created by Artem Orynko on 09.12.16.
 * Copyright © 2016 Qualaroo. All rights reserved.
 */

public class QMSurvey {

    private String alias;
    public String surveyId;
    public QMShowSurvey howOftenShowSurvey;
    public ArrayList<String> identity;

    public QMSurvey(String alias, String id) {
        this.alias = alias;
        this.surveyId = id;
        this.howOftenShowSurvey = QMShowSurvey.QualarooShowSurveyDefault;
    }
}
