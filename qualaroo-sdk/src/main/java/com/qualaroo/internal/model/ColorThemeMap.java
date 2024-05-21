package com.qualaroo.internal.model;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

import java.io.Serializable;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class ColorThemeMap implements Serializable {

    private final String backgroundColor;
    private final String dimType;
    private final String textColor;
    @Deprecated private final String buttonTextColor;
    private final String buttonEnabledColor;
    private final String buttonDisabledColor;
    private final String buttonTextEnabled;
    private final String buttonTextDisabled;
    private final String uiNormal;
    private final String uiSelected;
    private final Float dimOpacity;
    private  final  String npsBackgroundColor;
    private  final  String npsSelectedColor;
    private final  String ansColor;
    private  final  String ansSelectedColor;
    private  final  String buttonsRadius;


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

    @Nullable
    public Float dimOpacity() {
        return dimOpacity;
    }

    public String npsBackgroundColor() {
        return npsBackgroundColor;
    }
    public String npsSelectedColor() {
        return npsSelectedColor;
    }

    public String ansColor() {
        return ansColor;
    }
    public String ansSelectedColor() {
        return ansSelectedColor;
    }
    public String buttonsRadius() {
        return buttonsRadius;
    }

    @Deprecated public String buttonTextColor() {
        return buttonTextColor;
    }

    @VisibleForTesting ColorThemeMap(String backgroundColor,
                                     String dimType,
                                     String textColor,
                                     String buttonEnabledColor,
                                     String buttonDisabledColor,
                                     String buttonTextEnabled,
                                     String buttonTextDisabled,
                                     String uiNormal,
                                     String uiSelected,
                                     Float dimOpacity,
                                     String npsBackgroundColor,
                                     String npsSelectedColor,
                                     String ansColor,
                                     String ansSelectedColor,
                                     String buttonsRadius
                                     ) {
        this.backgroundColor = backgroundColor;
        this.dimType = dimType;
        this.textColor = textColor;
        this.buttonEnabledColor = buttonEnabledColor;
        this.buttonDisabledColor = buttonDisabledColor;
        this.buttonTextEnabled = buttonTextEnabled;
        this.buttonTextDisabled = buttonTextDisabled;
        this.uiNormal = uiNormal;
        this.uiSelected = uiSelected;
        this.dimOpacity = dimOpacity;
        this.buttonTextColor = null;
        this.npsBackgroundColor = npsBackgroundColor;
        this.npsSelectedColor = npsSelectedColor;
        this.ansColor = ansColor;
        this.ansSelectedColor = ansSelectedColor;
        this.buttonsRadius = buttonsRadius;
    }

    @SuppressWarnings("unused") private ColorThemeMap() {
        this.backgroundColor = null;
        this.dimType = null;
        this.textColor = null;
        this.buttonEnabledColor = null;
        this.buttonDisabledColor = null;
        this.buttonTextEnabled = null;
        this.buttonTextDisabled = null;
        this.uiNormal = null;
        this.uiSelected = null;
        this.dimOpacity = null;
        this.buttonTextColor = null;
        this.npsBackgroundColor = null;
        this.npsSelectedColor = null;
        this.ansColor = null;
        this.ansSelectedColor = null;
        this.buttonsRadius = null;
    }


}
