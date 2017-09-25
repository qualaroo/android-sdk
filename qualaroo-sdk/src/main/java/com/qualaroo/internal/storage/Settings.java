package com.qualaroo.internal.storage;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

public class Settings {

    private final SharedPreferences sharedPreferences;

    public Settings(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void store(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    @Nullable
    public String get(String key) {
        return sharedPreferences.getString(key, null);
    }
}
