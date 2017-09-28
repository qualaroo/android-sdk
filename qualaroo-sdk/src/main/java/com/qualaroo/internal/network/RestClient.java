package com.qualaroo.internal.network;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
            if(!response.isSuccessful()) {
                return Result.error(new HttpException(response.code()));
            }
            if (response.body() != null) {
                return Result.of(gson.fromJson(response.body().string(), resultClass));
            } else {
                return Result.of(gson.fromJson("", resultClass));
            }
        } catch (IOException e) {
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
