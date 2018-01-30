package com.qualaroo.sample

import android.app.Application
import com.qualaroo.Qualaroo

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Qualaroo.initializeWith(this)
                .setApiKey("API_KEY_HERE")
                .init()
    }
}