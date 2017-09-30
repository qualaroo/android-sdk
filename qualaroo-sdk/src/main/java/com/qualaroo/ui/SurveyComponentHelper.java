package com.qualaroo.ui;

import android.content.Context;
import android.support.annotation.RestrictTo;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
class SurveyComponentHelper {

    public static SurveyComponent get(Context context) {
        return (SurveyComponent) context.getSystemService(SurveyComponent.class.getName());
    }
}
