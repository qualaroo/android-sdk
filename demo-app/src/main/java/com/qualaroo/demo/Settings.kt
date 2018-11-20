/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.demo

import android.content.Context
import android.content.SharedPreferences

class Settings(context: Context) {

    private companion object {
        const val PREFS_NAME = "qualaroo_demo_prefs"
        const val KEY_API_KEY = "qualaroo_demo_api_key"
        const val KEY_RECENTLY_SHOWN_SURVEY = "qualaroo_demo_recent_survey"
        const val DEFAULT_STAGING_API_KEY = "API_KEY_HERE"
    }

    private val sharedPreferences: SharedPreferences

    init {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun storeApiKey(apiKey: String) {
        sharedPreferences.edit().putString(KEY_API_KEY, apiKey).commit()
    }

    fun apiKey(): String {
        return sharedPreferences.getString(KEY_API_KEY, DEFAULT_STAGING_API_KEY)
    }

    fun storeRecentSurveyAlias(alias: String) {
        sharedPreferences.edit().putString(KEY_RECENTLY_SHOWN_SURVEY, alias).apply()
    }

    fun recentSurveyAlias(): String {
        return sharedPreferences.getString(KEY_RECENTLY_SHOWN_SURVEY, "")
    }


}
