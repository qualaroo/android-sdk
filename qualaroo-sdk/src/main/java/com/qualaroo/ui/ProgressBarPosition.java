package com.qualaroo.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public enum ProgressBarPosition {
    NONE, TOP, BOTTOM;

    @NonNull public static ProgressBarPosition fromValue(@Nullable String value) {
        if ("top".equals(value)) return TOP;
        if ("bottom".equals(value)) return BOTTOM;
        return NONE;
    }
}
