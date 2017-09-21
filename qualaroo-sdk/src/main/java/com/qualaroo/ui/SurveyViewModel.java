package com.qualaroo.ui;

import android.support.annotation.ColorInt;

public class SurveyViewModel {

    private final @ColorInt int textColor;
    private final @ColorInt int backgroundColor;
    private final @ColorInt int buttonDisabledColor;
    private final boolean cannotBeClosed;
    private final boolean isFullscreen;

    public SurveyViewModel(int textColor, int backgroundColor, int buttonDisabledColor, boolean cannotBeClosed, boolean isFullscreen) {
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.buttonDisabledColor = buttonDisabledColor;
        this.cannotBeClosed = cannotBeClosed;
        this.isFullscreen = isFullscreen;
    }

    public boolean cannotBeClosed() {
        return cannotBeClosed;
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }

    public int textColor() {
        return textColor;
    }

    public int backgroundColor() {
        return backgroundColor;
    }

    public boolean iscannotBeClosed() {
        return cannotBeClosed;
    }

    public boolean isfullscreen() {
        return isFullscreen;
    }

    public int buttonDisabledColor() {
        return buttonDisabledColor;
    }
}
