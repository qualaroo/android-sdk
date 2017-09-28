package com.qualaroo.ui;

import android.content.Context;

class SurveyComponentHelper {

    public static SurveyComponent get(Context context) {
        return (SurveyComponent) context.getSystemService(SurveyComponent.class.getName());
    }
}
