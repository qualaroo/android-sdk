package com.qualaroo.util;

import android.text.Editable;
import android.text.TextWatcher;

public abstract class TextWatcherAdapter implements TextWatcher {
    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //noop
    }

    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
        //noop
    }

    @Override public void afterTextChanged(Editable s) {
        //noop
    }
}
