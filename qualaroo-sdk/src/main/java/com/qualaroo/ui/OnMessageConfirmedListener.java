package com.qualaroo.ui;

import android.support.annotation.RestrictTo;

import com.qualaroo.internal.model.Message;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public interface OnMessageConfirmedListener {
    void onMessageConfirmed(Message message);
}
