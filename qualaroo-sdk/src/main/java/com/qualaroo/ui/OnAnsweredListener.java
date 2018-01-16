package com.qualaroo.ui;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import com.qualaroo.internal.model.UserResponse;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public interface OnAnsweredListener {
    void onResponse(@NonNull UserResponse userResponse);
}
