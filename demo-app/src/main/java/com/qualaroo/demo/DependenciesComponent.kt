package com.qualaroo.demo

import com.google.gson.Gson
import com.qualaroo.QualarooLogger
import com.qualaroo.demo.repository.Credentials
import com.qualaroo.demo.repository.SurveyAliasesRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class DependenciesComponent(private val apiKey: String) {

    fun surveysRepository(): SurveyAliasesRepository {
        return SurveyAliasesRepository(okHttpClient(), gson(), credentials())
    }

    private fun credentials(): Credentials {
        return Credentials(apiKey)
    }

    private fun gson(): Gson {
        return Gson()
    }

    private fun okHttpClient(): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message -> QualarooLogger.info(message) })
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .build()
    }
}
