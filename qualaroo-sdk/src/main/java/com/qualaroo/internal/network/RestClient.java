/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.internal.network;

import android.support.annotation.RestrictTo;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.qualaroo.QualarooLogger;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class RestClient {

    private final OkHttpClient okHttpClient;
    private final Gson gson;

    public RestClient(OkHttpClient okHttpClient, Gson gson) {
        this.okHttpClient = okHttpClient;
        this.gson = gson;
    }

    <T> Result<T> get(HttpUrl httpUrl, Class<T> resultClass) {
        try {
            Request request = new Request.Builder()
                    .url(httpUrl)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                return Result.error(new HttpException(response.code()));
            }
            ResponseBody body = response.body();
            try {
                return Result.of(gson.fromJson(body.charStream(), resultClass));
            } finally {
                body.close();
            }
        } catch (IOException e) {
            return Result.error(e);
        } catch (JsonParseException e) {
            QualarooLogger.error("An unexpected error occurred while parsing server's response. Please get in touch with our customer support to help us solve the issue.");
            QualarooLogger.error(e);
            return Result.error(e);
        }
    }

    public Response get(HttpUrl httpUrl) throws IOException {
        Request request = new Request.Builder()
                .url(httpUrl)
                .build();
        return okHttpClient.newCall(request).execute();
    }
}
