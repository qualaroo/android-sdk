package com.qualaroo.util;

import android.content.Context;
import android.support.annotation.RestrictTo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class KeyboardUtil {

    public static void showKeyboard(EditText editText) {
        if (editText == null) {
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(editText, 0);
        }
    }

    public static void hideKeyboard(View view) {
        if (view == null) {
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private KeyboardUtil() {
        //no instances
    }



}
