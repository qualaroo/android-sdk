package com.qualaroo.ui;

import androidx.annotation.RestrictTo;

import com.qualaroo.internal.model.Message;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public interface OnMessageConfirmedListener {
    void onMessageConfirmed(Message message);
}
