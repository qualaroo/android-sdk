package com.qualaroo.internal.model;

import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;

import java.io.Serializable;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class ColorThemeMap implements Serializable {

    private final String backgroundColor;
    private final String dimType;
    private final String textColor;
    @Deprecated private final String buttonTextColor;
    private final String buttonEnabledColor;
    private final String buttonDisabledColor;
    private final String buttonTextEnabled;
    private final String buttonTextDisabled;
    private final String uiNormal;
    private final String uiSelected;

    public String dimType() {
        return dimType;
    }

    public String backgroundColor() {
        return backgroundColor;
    }

    public String textColor() {
        return textColor;
    }

    public String buttonEnabledColor() {
        return buttonEnabledColor;
    }

    public String buttonDisabledColor() {
        return buttonDisabledColor;
    }

    public String buttonTextEnabled() {
        return buttonTextEnabled;
    }

    public String buttonTextDisabled() {
        return buttonTextDisabled;
    }

    public String uiNormal() {
        return uiNormal;
    }

    public String uiSelected() {
        return uiSelected;
    }

    @Deprecated public String buttonTextColor() {
        return buttonTextColor;
    }

    @VisibleForTesting ColorThemeMap(String backgroundColor,
                                     String dimType,
                                     String textColor,
                                     String buttonEnabledColor,
                                     String buttonDisabledColor,
                                     String buttonTextEnabled,
                                     String buttonTextDisabled,
                                     String uiNormal,
                                     String uiSelected) {
        this.backgroundColor = backgroundColor;
        this.dimType = dimType;
        this.textColor = textColor;
        this.buttonEnabledColor = buttonEnabledColor;
        this.buttonDisabledColor = buttonDisabledColor;
        this.buttonTextEnabled = buttonTextEnabled;
        this.buttonTextDisabled = buttonTextDisabled;
        this.uiNormal = uiNormal;
        this.uiSelected = uiSelected;
        this.buttonTextColor = null;
    }

    @SuppressWarnings("unused") private ColorThemeMap() {
        this.backgroundColor = null;
        this.dimType = null;
        this.textColor = null;
        this.buttonEnabledColor = null;
        this.buttonDisabledColor = null;
        this.buttonTextEnabled = null;
        this.buttonTextDisabled = null;
        this.uiNormal = null;
        this.uiSelected = null;
        this.buttonTextColor = null;
    }
}
