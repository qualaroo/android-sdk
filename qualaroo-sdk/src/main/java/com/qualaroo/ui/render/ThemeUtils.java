package com.qualaroo.ui.render;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.RestrictTo;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.view.TintableBackgroundView;
import androidx.core.widget.CompoundButtonCompat;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.reflect.Field;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class ThemeUtils {

    private ThemeUtils() {
        throw new IllegalStateException("No instances");
    }

    public static void applyTheme(CompoundButton compoundButton, Theme theme) {
        int[][] states = new int[][]{
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_checked}
        };
        int[] colors = new int[]{theme.uiNormal(), theme.uiSelected()};
        CompoundButtonCompat.setButtonTintList(compoundButton, new ColorStateList(states, colors));
    }

    public static void applyTheme(EditText editText, Theme theme) {
        if (editText instanceof TintableBackgroundView) {
            int[][] states = new int[][]{new int[]{-android.R.attr.state_focused}, new int[]{android.R.attr.state_focused}};
            int[] colors = new int[]{theme.uiNormal(), theme.uiSelected()};
            ((TintableBackgroundView) editText).setSupportBackgroundTintList(new ColorStateList(states, colors));
        }
        editText.setTextColor(theme.textColor());
        setCursorDrawableColor(editText, theme.uiSelected());
    }

    private static void setCursorDrawableColor(EditText editText, @ColorInt int color) {
        try {
            @SuppressLint("SoonBlockedPrivateApi") Field cursorDrawableResField = TextView.class.getDeclaredField("mCursorDrawableRes");
            cursorDrawableResField.setAccessible(true);
            int cursorDrawableRes = cursorDrawableResField.getInt(editText);
            Field editorField = TextView.class.getDeclaredField("mEditor");
            editorField.setAccessible(true);
            Object editor = editorField.get(editText);
            Class<?> clazz = editor.getClass();
            Field cursorDrawableField = clazz.getDeclaredField("mCursorDrawable");
            cursorDrawableField.setAccessible(true);
            Drawable[] drawables = new Drawable[2];
            drawables[0] = editText.getContext().getResources().getDrawable(cursorDrawableRes);
            drawables[1] = editText.getContext().getResources().getDrawable(cursorDrawableRes);
            drawables[0].setColorFilter(color, PorterDuff.Mode.SRC_IN);
            drawables[1].setColorFilter(color, PorterDuff.Mode.SRC_IN);
            cursorDrawableField.set(editor, drawables);
        } catch (Throwable ignored) {
        }
    }

    static void applyTheme(Button button, Theme theme) {
        if (button instanceof TintableBackgroundView) {
            int[][] states = new int[][]{
                    new int[] {-android.R.attr.state_enabled},
                    new int[] {android.R.attr.state_enabled}
            };
            int[] colors = new int[] {
                    theme.buttonDisabledColor(),
                    theme.buttonEnabledColor(),
            };
            ((TintableBackgroundView) button).setSupportBackgroundTintList(new ColorStateList(states, colors));
        }
        int[][] states = new int[][] {
                new int[] {-android.R.attr.state_enabled},
                new int[] {android.R.attr.state_enabled}
        };
        int[] colors = new int[] {
                theme.buttonTextDisabled(),
                theme.buttonTextEnabled()
        };
        button.setTextColor(new ColorStateList(states, colors));
    }

    static void applyTheme(Spinner spinner, Theme theme) {
        if (spinner instanceof TintableBackgroundView) {
            int[][] states = new int[][]{
                    new int[] {android.R.attr.state_enabled},
                    new int[] {android.R.attr.state_enabled, android.R.attr.state_pressed}
            };
            int[] colors = new int[]{theme.uiNormal(), theme.uiSelected()};
            ((TintableBackgroundView) spinner).setSupportBackgroundTintList(new ColorStateList(states, colors));
        }
    }

    static void applyTheme(TextInputLayout textInputLayout, Theme theme) {
        setInputTextLayoutColor(textInputLayout, theme.uiNormal(), theme.uiSelected());
    }

    private static void setInputTextLayoutColor(TextInputLayout textInputLayout, @ColorInt int defaultColor, @ColorInt int focusedColor) {
        try {
            Field defaultTextColorField = TextInputLayout.class.getDeclaredField("mDefaultTextColor");
            defaultTextColorField.setAccessible(true);
            defaultTextColorField.set(textInputLayout, new ColorStateList(new int[][]{{0}}, new int[]{ defaultColor}));

            Field focusedTextColorField = TextInputLayout.class.getDeclaredField("mFocusedTextColor");
            focusedTextColorField.setAccessible(true);
            focusedTextColorField.set(textInputLayout, new ColorStateList(new int[][]{{0}}, new int[]{ focusedColor }));
        } catch (Exception ignored) {
            //ignore
        }
    }
}
