package com.qualaroo.ui;

import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
class SurveyViewModel {

    private final @ColorInt int textColor;
    private final @ColorInt int backgroundColor;
    private final @ColorInt int buttonDisabledColor;
    private final @ColorInt int dimColor;
    private final boolean cannotBeClosed;
    private final boolean isFullscreen;
    private final @Nullable String logoUrl;

    SurveyViewModel(int textColor, int backgroundColor, int buttonDisabledColor, int dimColor, boolean cannotBeClosed, boolean isFullscreen, @Nullable String logoUrl) {
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.buttonDisabledColor = buttonDisabledColor;
        this.dimColor = dimColor;
        this.cannotBeClosed = cannotBeClosed;
        this.isFullscreen = isFullscreen;
        this.logoUrl = logoUrl;
    }

    boolean cannotBeClosed() {
        return cannotBeClosed;
    }

    boolean isFullscreen() {
        return isFullscreen;
    }

    int textColor() {
        return textColor;
    }

    int backgroundColor() {
        return backgroundColor;
    }

    int buttonDisabledColor() {
        return buttonDisabledColor;
    }

    int dimColor() {
        return dimColor;
    }

    @Nullable public String logoUrl() {
        return logoUrl;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SurveyViewModel that = (SurveyViewModel) o;

        if (textColor != that.textColor) return false;
        if (backgroundColor != that.backgroundColor) return false;
        if (buttonDisabledColor != that.buttonDisabledColor) return false;
        if (dimColor != that.dimColor) return false;
        if (cannotBeClosed != that.cannotBeClosed) return false;
        return isFullscreen == that.isFullscreen;
    }

    @Override public int hashCode() {
        int result = textColor;
        result = 31 * result + backgroundColor;
        result = 31 * result + buttonDisabledColor;
        result = 31 * result + dimColor;
        result = 31 * result + (cannotBeClosed ? 1 : 0);
        result = 31 * result + (isFullscreen ? 1 : 0);
        return result;
    }
}
