package com.qualaroo.internal.model;

import android.support.annotation.VisibleForTesting;

import java.io.Serializable;

public final class ColorThemeMap implements Serializable {

    private String dimType;
    private String backgroundColor;
    private String borderColor;
    private String textColor;
    private String buttonTextColor;
    private String buttonEnabledColor;
    private String buttonDisabledColor;

    public String dimType() {
        return dimType;
    }

    public String backgroundColor() {
        return backgroundColor;
    }

    public String borderColor() {
        return borderColor;
    }

    public String textColor() {
        return textColor;
    }

    public String buttonTextColor() {
        return buttonTextColor;
    }

    public String buttonEnabledColor() {
        return buttonEnabledColor;
    }

    public String buttonDisabledColor() {
        return buttonDisabledColor;
    }

    @VisibleForTesting ColorThemeMap(String dimType, String backgroundColor, String borderColor, String textColor, String buttonTextColor, String buttonEnabledColor, String buttonDisabledColor) {
        this.dimType = dimType;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.textColor = textColor;
        this.buttonTextColor = buttonTextColor;
        this.buttonEnabledColor = buttonEnabledColor;
        this.buttonDisabledColor = buttonDisabledColor;
    }

    ColorThemeMap() {

    }
}
