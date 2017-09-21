package com.qualaroo.util;

import android.view.View;

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