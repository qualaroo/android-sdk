package com.qualaroo.ui.render;

import android.graphics.Color;
import android.support.annotation.ColorInt;

import com.qualaroo.internal.model.ColorThemeMap;

public final class Theme {

    public static Theme from(ColorThemeMap colorThemeMap) {
        Theme theme = new Theme();
        theme.dimType = parseColorSafely(colorThemeMap.dimType());
        theme.backgroundColor = parseColorSafely(colorThemeMap.backgroundColor());
        theme.borderColor = parseColorSafely(colorThemeMap.borderColor());
        theme.textColor = parseColorSafely(colorThemeMap.textColor());
        theme.buttonTextColor = parseColorSafely(colorThemeMap.buttonTextColor());
        theme.buttonEnabledColor = parseColorSafely(colorThemeMap.buttonEnabledColor());
        theme.buttonDisabledColor = parseColorSafely(colorThemeMap.buttonDisabledColor());
        theme.accentColor = parseColorSafely(colorThemeMap.buttonEnabledColor());
        return theme;
    }

    private static @ColorInt int parseColorSafely(String color) {
        try {
            return Color.parseColor(color);
        } catch (IllegalArgumentException | NullPointerException e) {
            return Color.BLACK;
        }
    }

    @ColorInt private int dimType;
    @ColorInt private int backgroundColor;
    @ColorInt private int borderColor;
    @ColorInt private int textColor;
    @ColorInt private int buttonTextColor;
    @ColorInt private int buttonEnabledColor;
    @ColorInt private int buttonDisabledColor;
    @ColorInt private int accentColor;

    public int dimType() {
        return dimType;
    }

    public int backgroundColor() {
        return backgroundColor;
    }

    public int borderColor() {
        return borderColor;
    }

    public int textColor() {
        return textColor;
    }

    public int buttonTextColor() {
        return buttonTextColor;
    }

    public int buttonEnabledColor() {
        return buttonEnabledColor;
    }

    public int buttonDisabledColor() {
        return buttonDisabledColor;
    }

    public int accentColor() {
        return buttonEnabledColor();
    }
}
