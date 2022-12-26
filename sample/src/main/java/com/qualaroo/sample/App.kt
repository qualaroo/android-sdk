/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.sample

import android.app.Application
import com.qualaroo.Qualaroo

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Qualaroo.initializeWith(this)
                .setApiKey("NzQ4MTQ6YTJkMzA3OTY4MWNjMWVmYWYzM2VjMDM0ZWY4Nzg2YzBlN2ExMjI2ZDo3Nzc3Mw==")
                .init()
    }
}
