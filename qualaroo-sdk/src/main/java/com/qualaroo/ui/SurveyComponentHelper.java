package com.qualaroo.ui;

import android.content.Context;
import androidx.annotation.RestrictTo;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
class SurveyComponentHelper {

    public static SurveyComponent get(Context context) {
        return (SurveyComponent) context.getSystemService(SurveyComponent.class.getName());
    }
}
