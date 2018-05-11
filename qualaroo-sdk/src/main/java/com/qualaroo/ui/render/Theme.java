package com.qualaroo.ui.render;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;

import com.qualaroo.internal.model.ColorThemeMap;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class Theme {

    public static Theme create(ColorThemeMap map) {
        if (isLegacyColorThemeMap(map)) {
            return parseLegacy(map);
        }
        return parse(map);
    }

    private static Theme parse(ColorThemeMap map) {
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
        Float dimOpacity = map.dimOpacity();
        if (dimOpacity != null) {
            theme.dimOpacity = dimOpacity;
        } else {
            theme.dimOpacity = 1.0f;
        }
        return theme;
    }

    private static Theme parseLegacy(ColorThemeMap map) {
        Theme theme = new Theme();
        theme.backgroundColor = parseColorSafely(map.backgroundColor());
        theme.dimColor = parseDimType(map.dimType());
        theme.textColor = parseColorSafely(map.textColor());
        theme.buttonEnabledColor = parseColorSafely(map.buttonEnabledColor());
        theme.buttonDisabledColor = parseColorSafely(map.buttonDisabledColor());
        theme.buttonTextEnabled = parseColorSafely(map.buttonTextColor());
        theme.buttonTextDisabled = parseColorSafely(map.buttonTextColor());
        theme.uiNormal = parseColorSafely(map.buttonDisabledColor());
        theme.uiSelected = parseColorSafely(map.buttonEnabledColor());
        theme.dimOpacity = 1.0f;
        return theme;
    }

    private static @ColorInt int parseColorSafely(String color) {
        try {
            return Color.parseColor(color);
        } catch (IllegalArgumentException | NullPointerException e) {
            return Color.BLACK;
        }
    }

    private static final String LIGHT_DIM_COLOR = "#D4CACED6";
    private static final String VERY_LIGHT_DIM_COLOR = "#D4FAFAFA";
    private static final String DARK_DIM_COLOR = "#D4323433";
    private static final String DEFAULT_DIM_COLOR = DARK_DIM_COLOR;

    private static @ColorInt int parseDimType(String dimType) {
        if (dimType == null) {
            return Color.parseColor(DEFAULT_DIM_COLOR);
        }
        switch(dimType) {
            case "light":
                return Color.parseColor(LIGHT_DIM_COLOR);
            case "very_light":
                return Color.parseColor(VERY_LIGHT_DIM_COLOR);
        }
        return Color.parseColor(DARK_DIM_COLOR);
    }

    private static boolean isLegacyColorThemeMap(ColorThemeMap map) {
        return map.uiNormal() == null ||
                map.uiSelected() == null ||
                map.buttonTextEnabled() == null ||
                map.buttonTextDisabled() == null;
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
    private float dimOpacity;

    private Theme() {
        //for static factory method
    }

    @VisibleForTesting Theme(int backgroundColor, int dimColor, int textColor, int buttonEnabledColor, int buttonDisabledColor, int buttonTextEnabled, int buttonTextDisabled, int uiNormal, int uiSelected, float dimOpacity) {
        this.backgroundColor = backgroundColor;
        this.dimColor = dimColor;
        this.textColor = textColor;
        this.buttonEnabledColor = buttonEnabledColor;
        this.buttonDisabledColor = buttonDisabledColor;
        this.buttonTextEnabled = buttonTextEnabled;
        this.buttonTextDisabled = buttonTextDisabled;
        this.uiNormal = uiNormal;
        this.uiSelected = uiSelected;
        this.dimOpacity = dimOpacity;
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

    @ColorInt public int buttonEnabledColor() {
        return buttonEnabledColor;
    }

    @ColorInt public int buttonDisabledColor() {
        return buttonDisabledColor;
    }

    @ColorInt public int buttonTextEnabled() {
        return buttonTextEnabled;
    }

    @ColorInt public int buttonTextDisabled() {
        return buttonTextDisabled;
    }

    @ColorInt public int uiNormal() {
        return uiNormal;
    }

    @ColorInt public int uiSelected() {
        return uiSelected;
    }

    public float dimOpacity() {
        return dimOpacity;
    }
}
