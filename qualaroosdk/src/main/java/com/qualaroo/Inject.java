package com.qualaroo;

import android.content.Context;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qualaroo.internal.SessionInfo;
import com.qualaroo.internal.UserInfo;
import com.qualaroo.internal.model.Language;
import com.qualaroo.internal.model.LanguageJsonDeserializer;
import com.qualaroo.internal.model.QuestionType;
import com.qualaroo.internal.model.QuestionTypeDeserializer;
import com.qualaroo.internal.network.ApiConfig;
import com.qualaroo.internal.network.RestClient;
import com.qualaroo.internal.network.SurveyClient;
import com.qualaroo.internal.storage.Settings;

import java.io.IOException;
import java.util.concurrent.Executors;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class Inject {

    public static Qualaroo qualaroo(Context context) {
        return new Qualaroo(surveyClient(context));
    }

    private static SurveyClient surveyClient(Context context) {
        final HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override public void log(String message) {
                Log.d("OkHttp", message);
            }
        });
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        final String authToken = Credentials.basic("39241", "752bd98ec21216303a92fb9e0f2f325f7e6a455b");
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(new Interceptor() {
                    @Override public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request().newBuilder()
                                .header("Authorization", authToken)
                                .build();
                        return chain.proceed(request);
                    }
                })
                .build();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Language.class, new LanguageJsonDeserializer())
                .registerTypeAdapter(QuestionType.class, new QuestionTypeDeserializer())
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        SessionInfo sessionInfo = new SessionInfo(context);
        Settings settings = new Settings(context);
        UserInfo userInfo = new UserInfo(settings);
        return new SurveyClient(new RestClient(okHttpClient, gson), new ApiConfig(), sessionInfo, userInfo, Executors.newSingleThreadExecutor());
    }

}
