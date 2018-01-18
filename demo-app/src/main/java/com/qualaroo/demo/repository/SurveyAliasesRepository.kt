package com.qualaroo.demo.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import qualaroo.com.QualarooMobileDemo.BuildConfig
import java.io.IOException
import java.io.Reader

class SurveyAliasesRepository(
        private val okHttpClient: OkHttpClient,
        private val gson: Gson,
        private val credentials: Credentials
) {

    companion object {
        const val PARAM_APP_ID = "app_id"
        val API_URL = if (BuildConfig.DEBUG) "staging-app.qualaroo.com" else "api.qualaroo.com"
    }

    fun fetchAliases(): Single<List<String>> {
        val url = HttpUrl.Builder()
                .scheme("https")
                .host(API_URL)
                .addPathSegments("api/v1.5/showcases")
                .addQueryParameter(PARAM_APP_ID, credentials.siteId())
                .build()
        val authToken = okhttp3.Credentials.basic(credentials.apiKey(), credentials.apiSecret())
        val request = Request.Builder()
                .url(url)
                .header("Authorization", authToken)
                .build()
        return Observable.fromCallable { execute(request) }
                .flatMap { Observable.fromIterable(it) }
                .map { it.alias }
                .toList()
    }

    private fun execute(request: Request): List<Survey> {
        val response = okHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
            response.body()?.let {
                it.charStream().use {
                    return gson.fromJson(it)
                }
            }
            return emptyList()
        } else {
            throw IOException("Response code: ${response.code()}")
        }
    }

    private inline fun <reified T> Gson.fromJson(json: Reader) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)
}
