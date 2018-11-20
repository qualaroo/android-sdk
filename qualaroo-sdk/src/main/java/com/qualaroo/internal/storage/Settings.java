/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.internal.storage;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class Settings {

    private final SharedPreferences sharedPreferences;

    public Settings(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void store(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    @Nullable public String get(String key) {
        return sharedPreferences.getString(key, null);
    }
}
