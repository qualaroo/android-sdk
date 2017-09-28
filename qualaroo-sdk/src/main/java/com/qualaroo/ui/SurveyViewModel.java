package com.qualaroo.ui;

import android.support.annotation.ColorInt;

public class SurveyViewModel {

    private final @ColorInt int textColor;
    private final @ColorInt int backgroundColor;
    private final @ColorInt int buttonDisabledColor;
    private final @ColorInt int dimColor;
    private final boolean cannotBeClosed;
    private final boolean isFullscreen;

    SurveyViewModel(int textColor, int backgroundColor, int buttonDisabledColor, int dimColor, boolean cannotBeClosed, boolean isFullscreen) {
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.buttonDisabledColor = buttonDisabledColor;
        this.dimColor = dimColor;
        this.cannotBeClosed = cannotBeClosed;
        this.isFullscreen = isFullscreen;
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

    public int dimColor() {
        return dimColor;
    }
}
