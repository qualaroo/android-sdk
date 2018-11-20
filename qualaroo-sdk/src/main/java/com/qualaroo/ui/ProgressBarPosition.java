/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.ui;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public enum ProgressBarPosition {
    NONE, TOP, BOTTOM;

    @NonNull public static ProgressBarPosition fromValue(@Nullable String value) {
        if ("top".equals(value)) return TOP;
        if ("bottom".equals(value)) return BOTTOM;
        return NONE;
    }
}
