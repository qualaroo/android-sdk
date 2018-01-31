package com.qualaroo.demo

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import com.jakewharton.processphoenix.ProcessPhoenix
import com.qualaroo.Qualaroo
import com.squareup.leakcanary.LeakCanary
import java.lang.Exception

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        if (ProcessPhoenix.isPhoenixProcess(this)) {
            return
        }
        initLeakCanary()
        initStrictMode()
        val settings = Settings(this)
        val apiKey = settings.apiKey()
        Log.d("Qualaroo Demo App", "Using API key: ${settings.apiKey()}")
        try {
            Qualaroo.initializeWith(this)
                    .setApiKey(apiKey)
                    .setDebugMode(true)
                    .init()
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid api key provided!", Toast.LENGTH_SHORT).show()
        }

    }
    private fun initLeakCanary() {
        if (!LeakCanary.isInAnalyzerProcess(this)) {
            LeakCanary.install(this)
        }
    }

    private fun initStrictMode() {
        val threadPolicy = StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        StrictMode.setThreadPolicy(threadPolicy)

        val vmPolicy = StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        StrictMode.setVmPolicy(vmPolicy)
    }
}
