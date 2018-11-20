/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.util;

import android.support.annotation.RestrictTo;
import android.view.View;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public abstract class DebouncingOnClickListener implements View.OnClickListener {

    private static final long BLOCK_TIME_IN_MILLIS = 600;

    private long lastClickTime;

    @Override public final void onClick(View v) {
        long now = System.currentTimeMillis();
        if (now - lastClickTime > BLOCK_TIME_IN_MILLIS) {
            lastClickTime = now;
            doClick(v);
        }
    }

    public abstract void doClick(View v);
}
