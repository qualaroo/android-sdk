package com.qualaroo.ui.render;

import android.graphics.Color;
import androidx.annotation.ColorInt;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

import com.qualaroo.internal.model.ColorThemeMap;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class Theme {

    public static Theme create(ColorThemeMap map) {
        try {
            if (isLegacyColorThemeMap(map)) {
                return parseLegacy(map);
            }
            return parse(map);
        } catch (Exception e) {
            return fallbackTheme();
        }
    }

    private static Theme parse(ColorThemeMap map) {
        Theme theme = new Theme();
        theme.backgroundColor = Color.parseColor(map.backgroundColor());
        theme.dimColor = parseDimType(map.dimType());
        theme.textColor = Color.parseColor(map.textColor());
        theme.buttonEnabledColor = Color.parseColor(map.buttonEnabledColor());
        theme.buttonDisabledColor = Color.parseColor(map.buttonDisabledColor());
        theme.buttonTextEnabled = Color.parseColor(map.buttonTextEnabled());
        theme.buttonTextDisabled = Color.parseColor(map.buttonTextDisabled());
        theme.uiNormal = Color.parseColor(map.uiNormal());
        theme.uiSelected = Color.parseColor(map.uiSelected());
        theme.npsBackgroundColor = Color.parseColor(map.npsBackgroundColor());
        theme.npsSelectedColor = Color.parseColor(map.npsSelectedColor());
        theme.ansSelectedColor = Color.parseColor(map.ansSelectedColor());
        theme.ansColor = Color.parseColor(map.ansColor());
        theme.buttonsRadius = map.buttonsRadius();
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
        theme.backgroundColor = Color.parseColor(map.backgroundColor());
        theme.dimColor = parseDimType(map.dimType());
        theme.textColor = Color.parseColor(map.textColor());
        theme.buttonEnabledColor = Color.parseColor(map.buttonEnabledColor());
        theme.buttonDisabledColor = Color.parseColor(map.buttonDisabledColor());
        theme.buttonTextEnabled = Color.parseColor(map.buttonTextColor());
        theme.buttonTextDisabled = Color.parseColor(map.buttonTextColor());
        theme.uiNormal = Color.parseColor(map.buttonDisabledColor());
        theme.uiSelected = Color.parseColor(map.buttonEnabledColor());
        theme.npsBackgroundColor = Color.parseColor(map.npsBackgroundColor());
        theme.npsSelectedColor = Color.parseColor(map.npsSelectedColor());
        theme.ansSelectedColor = Color.parseColor(map.ansSelectedColor());
        theme.ansColor = Color.parseColor(map.ansColor());
        theme.buttonsRadius = map.buttonsRadius();
        theme.dimOpacity = 1.0f;
        return theme;
    }


    private static Theme fallbackTheme() {
        Theme theme = new Theme();
        theme.backgroundColor = Color.WHITE;
        theme.dimColor = Color.parseColor(DARK_DIM_COLOR);
        theme.textColor = Color.BLACK;
        theme.buttonEnabledColor = Color.DKGRAY;
        theme.buttonDisabledColor = Color.LTGRAY;
        theme.buttonTextEnabled = Color.BLACK;
        theme.buttonTextDisabled = Color.BLACK;
        theme.uiNormal = Color.DKGRAY;
        theme.uiSelected = Color.BLACK;
        theme.dimOpacity = 1.0f;
        theme.npsBackgroundColor = Color.BLACK;
        theme.npsSelectedColor = Color.BLACK;
        theme.ansSelectedColor = Color.BLACK;
        theme.ansColor = Color.BLACK;
        theme.buttonsRadius = "13px";
        return theme;
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

    private int npsBackgroundColor;
    private int npsSelectedColor;
    private int ansColor;
    private int ansSelectedColor;
    private String buttonsRadius;

    private Theme() {
        //for static factory methods
    }

    @VisibleForTesting Theme(int backgroundColor, int dimColor, int textColor, int buttonEnabledColor, int buttonDisabledColor, int buttonTextEnabled, int buttonTextDisabled, int uiNormal, int uiSelected, float dimOpacity,
                             int npsBackgroundColor, int npsSelectedColor, int ansSelectedColor, int ansColor, String buttonsRadius) {
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
        this.npsBackgroundColor = npsBackgroundColor;
        this.npsSelectedColor = npsSelectedColor;
        this.ansSelectedColor = ansSelectedColor;
        this.ansColor = ansColor;
        this.buttonsRadius = buttonsRadius;
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

    @ColorInt public int npsBackgroundColor() {
        return npsBackgroundColor;
    }
    @ColorInt public int npsSelectedColor() {
        return npsSelectedColor;
    }
    @ColorInt public int ansSelectedColor() {
        return ansSelectedColor;
    }
    @ColorInt public int ansColor() {
        return ansColor;
    }
    public String buttonsRadius() {
        return buttonsRadius;
    }
}
