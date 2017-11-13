package com.qualaroo.ui.render;

import android.content.res.ColorStateList;
import android.support.annotation.ColorInt;
import android.support.annotation.RestrictTo;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.TintableBackgroundView;
import android.support.v4.widget.TintableCompoundButton;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import java.lang.reflect.Field;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
class ThemeUtils {

    private ThemeUtils() {
        throw new IllegalStateException("No instances");
    }

    static void applyTheme(RadioButton radioButton, Theme theme) {
        if (radioButton instanceof TintableCompoundButton) {
            int[][] states = new int[][]{new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked}};
            int[] colors = new int[]{theme.buttonDisabledColor(), theme.accentColor()};
            ((TintableCompoundButton) radioButton).setSupportButtonTintList(new ColorStateList(states, colors));
        }
    }

    static void applyTheme(CheckBox checkBox, Theme theme) {
        if (checkBox instanceof TintableCompoundButton) {
            int[][] states = new int[][]{new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked}};
            int[] colors = new int[]{theme.buttonDisabledColor(), theme.accentColor()};
            ((TintableCompoundButton) checkBox).setSupportButtonTintList(new ColorStateList(states, colors));
        }
    }

    static void applyTheme(EditText editText, Theme theme) {
        if (editText instanceof TintableBackgroundView) {
            int[][] states = new int[][]{new int[]{-android.R.attr.state_focused}, new int[]{android.R.attr.state_focused}};
            int[] colors = new int[]{theme.buttonDisabledColor(), theme.accentColor()};
            ((TintableBackgroundView) editText).setSupportBackgroundTintList(new ColorStateList(states, colors));
        }
        editText.setTextColor(theme.textColor());
    }

    static void applyTheme(Button button, Theme theme) {
        if (button instanceof TintableBackgroundView) {
            int[][] states = new int[][]{
                    new int[] {-android.R.attr.state_enabled},
                    new int[] {android.R.attr.state_enabled},
                    new int[] {android.R.attr.state_enabled, android.R.attr.state_pressed}
            };
            int[] colors = new int[] {
                    theme.buttonDisabledColor(),
                    theme.buttonEnabledColor(),
                    theme.accentColor()
            };
            ((TintableBackgroundView) button).setSupportBackgroundTintList(new ColorStateList(states, colors));
        }
    }

    static void applyTheme(Spinner spinner, Theme theme) {
        if (spinner instanceof TintableBackgroundView) {
            int[][] states = new int[][]{
                    new int[] {android.R.attr.state_enabled},
                    new int[] {android.R.attr.state_enabled, android.R.attr.state_pressed}
            };
            int[] colors = new int[] {
                    theme.buttonDisabledColor(),
                    theme.accentColor()
            };
            ((TintableBackgroundView) spinner).setSupportBackgroundTintList(new ColorStateList(states, colors));
        }
    }

    static void applyTheme(TextInputLayout textInputLayout, Theme theme) {
        setInputTextLayoutColor(textInputLayout, theme.buttonDisabledColor(), theme.accentColor());
    }

    private static void setInputTextLayoutColor(TextInputLayout textInputLayout, @ColorInt int defaultColor, @ColorInt int focusedColor) {
        try {
            Field fDefaultTextColor = TextInputLayout.class.getDeclaredField("mDefaultTextColor");
            fDefaultTextColor.setAccessible(true);
            fDefaultTextColor.set(textInputLayout, new ColorStateList(new int[][]{{0}}, new int[]{ defaultColor}));

            Field fFocusedTextColor = TextInputLayout.class.getDeclaredField("mFocusedTextColor");
            fFocusedTextColor.setAccessible(true);
            fFocusedTextColor.set(textInputLayout, new ColorStateList(new int[][]{{0}}, new int[]{ focusedColor }));
        } catch (Exception ignored) {
            //ignore
        }
    }
}
