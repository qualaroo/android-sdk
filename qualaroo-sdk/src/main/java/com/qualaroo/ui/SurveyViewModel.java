package com.qualaroo.ui;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
class SurveyViewModel {

    private final @ColorInt int textColor;
    private final @ColorInt int backgroundColor;
    private final @ColorInt int uiNormal;
    private final @ColorInt int uiSelected;
    private final @ColorInt int dimColor;
    private final float dimOpacity;
    private final boolean cannotBeClosed;
    private final boolean isFullscreen;
    private final @Nullable String logoUrl;
    private final ProgressBarPosition progressBarPosition;

    SurveyViewModel(int textColor, int backgroundColor, int uiNormal, int uiSelected, int dimColor, float dimOpacity, boolean cannotBeClosed, boolean isFullscreen, @Nullable String logoUrl, ProgressBarPosition progressBarPosition) {
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.uiNormal = uiNormal;
        this.uiSelected = uiSelected;
        this.dimColor = dimColor;
        this.dimOpacity = dimOpacity;
        this.cannotBeClosed = cannotBeClosed;
        this.isFullscreen = isFullscreen;
        this.logoUrl = logoUrl;
        this.progressBarPosition = progressBarPosition;
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

    int dimColor() {
        return dimColor;
    }

    public float dimOpacity() {
        return dimOpacity;
    }

    public int uiNormal() {
        return uiNormal;
    }

    public int uiSelected() {
        return uiSelected;
    }

    public ProgressBarPosition progressBarPosition() {
        return progressBarPosition;
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
        if (uiNormal != that.uiNormal) return false;
        if (uiSelected != that.uiSelected) return false;
        if (dimColor != that.dimColor) return false;
        if (cannotBeClosed != that.cannotBeClosed) return false;
        if (isFullscreen != that.isFullscreen) return false;
        return logoUrl != null ? logoUrl.equals(that.logoUrl) : that.logoUrl == null;
    }

    @Override public int hashCode() {
        int result = textColor;
        result = 31 * result + backgroundColor;
        result = 31 * result + uiNormal;
        result = 31 * result + uiSelected;
        result = 31 * result + dimColor;
        result = 31 * result + (cannotBeClosed ? 1 : 0);
        result = 31 * result + (isFullscreen ? 1 : 0);
        result = 31 * result + (logoUrl != null ? logoUrl.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "SurveyViewModel{" +
                "textColor=" + textColor +
                ", backgroundColor=" + backgroundColor +
                ", uiNormal=" + uiNormal +
                ", uiSelected=" + uiSelected +
                ", dimColor=" + dimColor +
                ", cannotBeClosed=" + cannotBeClosed +
                ", isFullscreen=" + isFullscreen +
                ", logoUrl='" + logoUrl + '\'' +
                '}';
    }
}
