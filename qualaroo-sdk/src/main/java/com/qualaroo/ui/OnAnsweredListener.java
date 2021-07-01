package com.qualaroo.ui;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.qualaroo.internal.model.UserResponse;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public interface OnAnsweredListener {
    void onResponse(@NonNull UserResponse userResponse);
}
