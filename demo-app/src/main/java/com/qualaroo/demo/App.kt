package com.qualaroo.demo

import android.app.Application
import android.widget.Toast

import com.jakewharton.processphoenix.ProcessPhoenix
import com.qualaroo.Qualaroo
import java.lang.Exception

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        if (ProcessPhoenix.isPhoenixProcess(this)) {
            return
        }
        val settings = Settings(this)
        val apiKey = settings.apiKey()
        try {
            Qualaroo.initializeWith(this)
                    .setApiKey(apiKey)
                    .setDebugMode(true)
                    .init()
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid api key provided!", Toast.LENGTH_SHORT).show()
        }

    }
}
