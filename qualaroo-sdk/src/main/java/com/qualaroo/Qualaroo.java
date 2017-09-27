package com.qualaroo;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qualaroo.internal.Credentials;
import com.qualaroo.internal.SessionInfo;
import com.qualaroo.internal.SurveyDisplayQualifier;
import com.qualaroo.internal.TimeMatcher;
import com.qualaroo.internal.TimeProvider;
import com.qualaroo.internal.UserInfo;
import com.qualaroo.internal.UserPropertiesMatcher;
import com.qualaroo.internal.executor.UiThreadExecutor;
import com.qualaroo.internal.model.Language;
import com.qualaroo.internal.model.LanguageJsonDeserializer;
import com.qualaroo.internal.model.QuestionType;
import com.qualaroo.internal.model.QuestionTypeDeserializer;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.network.ApiConfig;
import com.qualaroo.internal.network.RestClient;
import com.qualaroo.internal.network.SurveysRepository;
import com.qualaroo.internal.storage.DatabaseLocalStorage;
import com.qualaroo.internal.storage.LocalStorage;
import com.qualaroo.internal.storage.Settings;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class Qualaroo implements QualarooSdk {

    private static QualarooSdk INSTANCE;

    public static QualarooSdk getInstance() {
        return INSTANCE;
    }

    private SurveyDisplayQualifier surveyDisplayQualifier;

    public static Builder with(Context context) {
        return new Builder(context);
    }

    private final Context context;
    private final SurveysRepository surveysRepository;
    private final Executor uiExecutor;
    private final Executor backgroundExecutor;

    private final AtomicBoolean requestingForSurvey = new AtomicBoolean(false);

    private Qualaroo(Context context, Credentials credentials, boolean debugMode) {
        this.context = context.getApplicationContext();
        this.uiExecutor = new UiThreadExecutor();
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
        LocalStorage localStorage = new DatabaseLocalStorage(this.context);
        SharedPreferences sharedPreferences = context.getSharedPreferences("qualaroo_prefs", Context.MODE_PRIVATE);
        Settings settings = new Settings(sharedPreferences);
        UserInfo userInfo = new UserInfo(settings, localStorage);
        UserPropertiesMatcher userPropertiesMatcher = new UserPropertiesMatcher(userInfo);
        TimeMatcher timeMatcher = new TimeMatcher(new TimeProvider());
        this.surveyDisplayQualifier = new SurveyDisplayQualifier(localStorage, userPropertiesMatcher, timeMatcher);

        RestClient restClient = buildRestClient(credentials);
        SessionInfo sessionInfo = new SessionInfo(this.context);
        surveysRepository = new SurveysRepository(credentials.siteId(), restClient, new ApiConfig(), sessionInfo, userInfo, TimeUnit.HOURS.toMillis(1));
    }


    @Override public void showSurvey(@NonNull final String alias) {
        if (alias.length() == 0) {
            throw new IllegalArgumentException("Alias can't be null or empty!");
        }
        if (requestingForSurvey.get()) {
            return;
        }
        requestingForSurvey.set(true);
        backgroundExecutor.execute(new Runnable() {
            @Override public void run() {
                List<Survey> surveys = surveysRepository.getSurveys();
                for (final Survey survey : surveys) {
                    if (alias.equals(survey.canonicalName())) {
                        boolean shouldShowSurvey = surveyDisplayQualifier.shouldShowSurvey(survey);
                        if (shouldShowSurvey) {
                            uiExecutor.execute(new Runnable() {
                                @Override public void run() {
                                    QualarooActivity.showSurvey(context, survey);
                                }
                            });
                            break;
                        }
                    }
                }
                requestingForSurvey.set(false);
            }
        });
    }

    @Override public void setUserId(@NonNull String userId) {

    }

    @Override public void setUserProperty(@NonNull String key, String value) {

    }

    @Override public void setPreferredLanguage(@NonNull String iso2Language) {

    }

    private RestClient buildRestClient(Credentials credentials) {
        Gson gson = buildGson();
        OkHttpClient okHttpClient = buildOkHttpClient(credentials);
        return new RestClient(okHttpClient, gson);
    }

    private OkHttpClient buildOkHttpClient(Credentials credentials) {
        final HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override public void log(String message) {
                Log.d("OkHttp", message);
            }
        });
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        final String authToken = okhttp3.Credentials.basic(credentials.apiKey(), credentials.apiSecret());
        return new OkHttpClient.Builder()
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
    }

    private Gson buildGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Language.class, new LanguageJsonDeserializer())
                .registerTypeAdapter(QuestionType.class, new QuestionTypeDeserializer())
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    public static class Builder {
        private final Context context;
        private Credentials credentials;
        private boolean debugMode = false;

        Builder(Context context) {
            this.context = context;
        }

        public Builder setApiKey(String apiKey) {
            this.credentials = new Credentials(apiKey);
            return this;
        }

        public Builder setDebugMode(boolean debugMode) {
            this.debugMode = debugMode;
            return this;
        }

        public void init() {
            if (INSTANCE == null) {
                INSTANCE = new Qualaroo(context, credentials, debugMode);
            }
        }
    }

}
