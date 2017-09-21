package com.qualaroo.internal.network;

import okhttp3.HttpUrl;

public class ApiConfig {

    private static final String QUALAROO = "testapp1.qualaroo.com";
    private static final String REPORT_API = "stage1.turbo.qualaroo.com";

    HttpUrl qualarooApi() {
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
