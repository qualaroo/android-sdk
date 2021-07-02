/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.demo

import android.content.Context
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager

//import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.processphoenix.ProcessPhoenix
import com.qualaroo.Qualaroo
import com.qualaroo.QualarooSurveyEventReceiver
import com.qualaroo.demo.dialog.AddUserPropertyDialog
import com.qualaroo.demo.dialog.LogsDialog
import com.qualaroo.demo.repository.SurveyAliasesRepository
import com.qualaroo.demo.util.Logcat
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import qualaroo.com.QualarooMobileDemo.R

class SurveyDemoActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()
    private val aliasSpinner by lazy { findViewById<Spinner>(R.id.qualaroo__demo_survey_alias_spinner) }
    private val aliasSpinnerProgressBar by lazy { findViewById<ProgressBar>(R.id.qualaroo__demo_survey_alias_spinner_progress_bar) }
    private val showButton by lazy { findViewById<View>(R.id.qualaroo__demo_show_survey) }

    private var aliasAdapter: ArrayAdapter<String>? = null
    private lateinit var surveyAliasesRepository: SurveyAliasesRepository
    private val broadcastReceiver = object : QualarooSurveyEventReceiver() {
        override fun onSurveyEvent(surveyAlias: String, eventType: Int) {
            when (eventType) {
                EVENT_TYPE_SHOWN -> Log.d("Observer", "$surveyAlias has been shown")
                EVENT_TYPE_DISMISSED -> Log.d("Observer", "$surveyAlias has been dismissed")
                EVENT_TYPE_FINISHED -> Log.d("Observer", "$surveyAlias has been finished")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logcat.clearLogcat()
        setContentView(R.layout.activity_survey)
        val settings = Settings(this)

        val apiKey = settings.apiKey()
        val apiKeyEditText = findViewById<EditText>(R.id.qualaroo__demo_api_key_edit_text)
        apiKeyEditText.setText(apiKey)

        surveyAliasesRepository = DependenciesComponent(apiKey).surveysRepository()

        findViewById<View>(R.id.qualaroo__demo_set_api_key_button).setOnClickListener {
            settings.storeApiKey(apiKeyEditText.text.toString())
            ProcessPhoenix.triggerRebirth(this@SurveyDemoActivity)
        }

        aliasAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        aliasSpinner.adapter = aliasAdapter
        showButton.setOnClickListener {
            val alias = aliasSpinner.selectedItem as String
            settings.storeRecentSurveyAlias(alias)
            showSurvey(alias)
        }
        findViewById<View>(R.id.qualaroo__demo_reset_surveys).setOnClickListener { clearAndRestartApp() }
        findViewById<View>(R.id.qualaroo__demo_set_user_property).setOnClickListener { showAddUserPropertyDialog() }
        findViewById<View>(R.id.qualaroo__demo_check_logs).setOnClickListener { showLogsDialog() }

        loadAvailableAliases()
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, QualarooSurveyEventReceiver.intentFilter())
    }

    private fun loadAvailableAliases() {
        aliasSpinner.hide()
        showButton.hide()
        aliasSpinnerProgressBar.show()
        val disposable = surveyAliasesRepository.fetchAliases()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ populateAdapter(it) }, { handleError(it) })
        compositeDisposable.add(disposable)
    }

    private fun populateAdapter(aliases: List<String>) {
        aliasSpinnerProgressBar.hide()
        aliasSpinner.show()
        showButton.show()
        aliasAdapter?.addAll(aliases)
    }

    private fun handleError(throwable: Throwable) {
        Log.e("Demo", "Exception caught!", throwable)
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

    private fun View.show() {
        visibility = View.VISIBLE
    }

    private fun View.hide() {
        visibility = View.GONE
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }
}
