package com.qualaroo.internal.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

public class Settings {

    private static final String PREF_NAME = "qualaroo_prefs";

    private final SharedPreferences sharedPreferences;

    public Settings(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void store(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    @Nullable
    public String get(String key) {
        return sharedPreferences.getString(key, null);
    }
}
