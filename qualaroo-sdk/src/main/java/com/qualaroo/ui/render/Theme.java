package com.qualaroo.ui.render;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;

import com.qualaroo.internal.model.ColorThemeMap;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class Theme {

    public static Theme from(ColorThemeMap map) {
        Theme theme = new Theme();
        theme.backgroundColor = parseColorSafely(map.backgroundColor());
        theme.dimColor = parseDimType(map.dimType());
        theme.textColor = parseColorSafely(map.textColor());
        theme.buttonEnabledColor = parseColorSafely(map.buttonEnabledColor());
        theme.buttonDisabledColor = parseColorSafely(map.buttonDisabledColor());
        theme.buttonTextEnabled = parseColorSafely(map.buttonTextEnabled());
        theme.buttonTextDisabled = parseColorSafely(map.buttonTextDisabled());
        theme.uiNormal = parseColorSafely(map.uiNormal());
        theme.uiSelected = parseColorSafely(map.uiSelected());
        return theme;
    }

    private static @ColorInt int parseColorSafely(String color) {
        try {
            return Color.parseColor(color);
        } catch (IllegalArgumentException | NullPointerException e) {
            return Color.BLACK;
        }
    }

    private static @ColorInt int parseDimType(String dimType) {
        if (dimType == null) {
            return Color.parseColor("#D4323433");
        }
        switch(dimType) {
            case "light":
                return Color.parseColor("#D4CACED6");
            case "very_light":
                return Color.parseColor("#D4FAFAFA");
        }
        return Color.parseColor("#D4323433");
    }

    private int backgroundColor;
    private int dimColor;
    private int textColor;
    private int buttonEnabledColor;
    private int buttonDisabledColor;
    private int buttonTextEnabled;
    private int buttonTextDisabled;
    private int uiNormal;
    private int uiSelected;

    private Theme() {
        //for static factory method
    }

    @VisibleForTesting Theme(int backgroundColor, int dimColor, int textColor, int buttonEnabledColor, int buttonDisabledColor, int buttonTextEnabled, int buttonTextDisabled, int uiNormal, int uiSelected) {
        this.backgroundColor = backgroundColor;
        this.dimColor = dimColor;
        this.textColor = textColor;
        this.buttonEnabledColor = buttonEnabledColor;
        this.buttonDisabledColor = buttonDisabledColor;
        this.buttonTextEnabled = buttonTextEnabled;
        this.buttonTextDisabled = buttonTextDisabled;
        this.uiNormal = uiNormal;
        this.uiSelected = uiSelected;
    }

    @ColorInt public int backgroundColor() {
        return backgroundColor;
    }

    @ColorInt public int dimColor() {
        return dimColor;
    }

    @ColorInt public int textColor() {
        return textColor;
    }

    @ColorInt int buttonEnabledColor() {
        return buttonEnabledColor;
    }

    @ColorInt int buttonDisabledColor() {
        return buttonDisabledColor;
    }

    @ColorInt int buttonTextEnabled() {
        return buttonTextEnabled;
    }

    @ColorInt int buttonTextDisabled() {
        return buttonTextDisabled;
    }

    @ColorInt public int uiNormal() {
        return uiNormal;
    }

    @ColorInt public int uiSelected() {
        return uiSelected;
    }
}
