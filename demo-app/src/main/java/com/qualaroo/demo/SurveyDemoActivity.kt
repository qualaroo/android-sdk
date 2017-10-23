package com.qualaroo.demo

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.jakewharton.processphoenix.ProcessPhoenix
import com.qualaroo.Qualaroo
import com.qualaroo.demo.dialog.AddUserPropertyDialog
import com.qualaroo.demo.dialog.LogsDialog
import com.qualaroo.demo.util.Logcat
import com.qualaroo.demo.util.TextWatcherAdapter
import qualaroo.com.QualarooMobileDemo.R

class SurveyDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logcat.clearLogcat()
        setContentView(R.layout.activity_survey)
        val settings = Settings(this)

        val apiKey = settings.apiKey() ?: "API_KEY_HERE"
        val apiKeyEditText = findViewById<EditText>(R.id.qualaroo__demo_api_key_edit_text)
        apiKeyEditText.setText(apiKey)

        findViewById<View>(R.id.qualaroo__demo_set_api_key_button).setOnClickListener {
            settings.storeApiKey(apiKeyEditText.text.toString())
            ProcessPhoenix.triggerRebirth(this@SurveyDemoActivity)
        }

        val showSurveyButton = findViewById<View>(R.id.qualaroo__demo_show_survey)
        val surveyAlias = findViewById<EditText>(R.id.qualaroo__demo_survey_alias_edit_text)
        surveyAlias.addTextChangedListener(object : TextWatcherAdapter() {
            override fun afterTextChanged(s: Editable) {
                showSurveyButton.isEnabled = s.isNotEmpty()
            }
        })
        surveyAlias.setText(settings.recentSurveyAlias())
        showSurveyButton.setOnClickListener {
            val alias = surveyAlias.text.toString()
            settings.storeRecentSurveyAlias(alias)
            showSurvey(alias)
        }

        findViewById<View>(R.id.qualaroo__demo_reset_surveys).setOnClickListener { clearAndRestartApp() }
        findViewById<View>(R.id.qualaroo__demo_set_user_property).setOnClickListener { showAddUserPropertyDialog() }
        findViewById<View>(R.id.qualaroo__demo_check_logs).setOnClickListener { showLogsDialog() }
    }

    private fun showSurvey(alias: String) {
        try {
            Qualaroo.getInstance().showSurvey(alias)
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Crash occured. Did you set a proper API key?", Toast.LENGTH_SHORT).show()
            Log.e("Demo", e.message, e)
        }
    }

    private fun showLogsDialog() {
        LogsDialog().show(supportFragmentManager, "logsDialog")
    }

    private fun showAddUserPropertyDialog() {
        AddUserPropertyDialog().show(supportFragmentManager, "propertyDialog")
    }

    private fun clearAndRestartApp() {
        applicationContext.deleteDatabase("qualaroo.db")
        applicationContext.getSharedPreferences("qualaroo_prefs", Context.MODE_PRIVATE).edit().clear().apply()
        ProcessPhoenix.triggerRebirth(this@SurveyDemoActivity)
    }
}
