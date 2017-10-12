package com.qualaroo.demo

import android.content.Context
import android.content.SharedPreferences

class Settings(context: Context) {

    internal companion object {
        private val PREFS_NAME = "qualaroo_demo_prefs"
        private val KEY_API_KEY = "qualaroo_demo_api_key"
        private val KEY_RECENTLY_SHOWN_SURVEY = "qualaroo_demo_recent_survey"
    }

    private val sharedPreferences: SharedPreferences

    init {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun storeApiKey(apiKey: String) {
        sharedPreferences.edit().putString(KEY_API_KEY, apiKey).commit()
    }

    fun apiKey(): String {
        return sharedPreferences.getString(KEY_API_KEY, "")
    }

    fun storeRecentSurveyAlias(alias: String) {
        sharedPreferences.edit().putString(KEY_RECENTLY_SHOWN_SURVEY, alias).commit()
    }

    fun recentSurveyAlias(): String {
        return sharedPreferences.getString(KEY_RECENTLY_SHOWN_SURVEY, "")
    }


}
