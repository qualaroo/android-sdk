package qualaroo.com.AndroidMobileSDK.Model;

import java.util.ArrayList;

import qualaroo.com.AndroidMobileSDK.QMShowSurvey;

/**
 * Created by Artem Orynko on 09.12.16.
 * Copyright © 2016 Qualaroo. All rights reserved.
 */

public class QMSurvey {
    public String alias;
    public String nudgeID;
    public QMShowSurvey howOftenShowSurvey;
    public ArrayList<String> identity;

    public QMSurvey(String alias, String id) {
        this.alias = alias;
        this.nudgeID = id;
    }

    public void setHowOftenShowSurvey(QMShowSurvey howOftenShowSurvey) {
        this.howOftenShowSurvey = howOftenShowSurvey;
    }
}
