package com.qualaroo.sample

import android.app.Application
import com.qualaroo.Qualaroo

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Qualaroo.initializeWith(this)
                .setApiKey("<your_api_key_here>")
                .init()
    }
}