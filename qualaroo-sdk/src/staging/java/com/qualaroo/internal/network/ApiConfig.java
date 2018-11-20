/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.internal.network;

import okhttp3.HttpUrl;

public class ApiConfig {

    private static final String QUALAROO = "staging-app.qualaroo.com";
    private static final String REPORT_API = "stage1.turbo.qualaroo.com";

    public HttpUrl qualarooApi() {
        return new HttpUrl.Builder()
                .scheme("https")
                .host(QUALAROO)
                .addPathSegment("api")
                .addPathSegment("v1.5")
                .build();
    }

    HttpUrl reportApi() {
        return new HttpUrl.Builder()
                .scheme("https")
                .host(REPORT_API)
                .build();
    }
}
