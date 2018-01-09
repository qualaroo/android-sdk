package com.qualaroo.internal.model;

import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;

import java.io.Serializable;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class ColorThemeMap implements Serializable {

    private String backgroundColor;
    private String dimType;
    private String textColor;
    private String buttonEnabledColor;
    private String buttonDisabledColor;
    private String buttonTextEnabled;
    private String buttonTextDisabled;
    private String uiNormal;
    private String uiSelected;

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

    @VisibleForTesting ColorThemeMap(String backgroundColor, String dimType, String textColor, String buttonEnabledColor, String buttonDisabledColor, String buttonTextEnabled, String buttonTextDisabled, String uiNormal, String uiSelected) {
        this.backgroundColor = backgroundColor;
        this.dimType = dimType;
        this.textColor = textColor;
        this.buttonEnabledColor = buttonEnabledColor;
        this.buttonDisabledColor = buttonDisabledColor;
        this.buttonTextEnabled = buttonTextEnabled;
        this.buttonTextDisabled = buttonTextDisabled;
        this.uiNormal = uiNormal;
        this.uiSelected = uiSelected;
    }

    ColorThemeMap() {

    }
}
