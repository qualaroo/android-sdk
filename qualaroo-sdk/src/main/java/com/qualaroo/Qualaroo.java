package com.qualaroo;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qualaroo.internal.Credentials;
import com.qualaroo.internal.ReportManager;
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
import com.qualaroo.internal.network.ReportClient;
import com.qualaroo.internal.network.RestClient;
import com.qualaroo.internal.network.SurveysRepository;
import com.qualaroo.internal.storage.DatabaseLocalStorage;
import com.qualaroo.internal.storage.LocalStorage;
import com.qualaroo.internal.storage.Settings;
import com.qualaroo.ui.SurveyComponent;

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

    /**
     * Starts initialization phase of the SDK.
     * Make sure to call {@link QualarooSdk.Builder#init()} to finish initialization properly.
     * @param context - application {@link Context}
     * @return {@link QualarooSdk.Builder} that you can use to configure the SDK.
     */
    public static Builder initializeWith(Context context) {
        return new Builder(context);
    }

    /**
     * Returns an instance of QualarooSdk that you can use.
     * Make sure to initialize it first with:
     * {@link #initializeWith(Context) initializeWith} method calls.
     *
     * Example of initialization:
     * Qualaroo.initializeWith(getApplicationContext())
     *      .setApiKey("my_own_api_key")
     *      .setDebugMode(false)
     *      .init()
     *
     * @throws IllegalStateException when SDK was not initialized before
     * @return current instance of {@link QualarooSdk}
     */
    public static QualarooSdk getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException(
                    "Qualaroo SDK has not been properly initialized. Make sure you finish initalizeWith");
        }
        return INSTANCE;
    }

    private static QualarooSdk INSTANCE;

    private final UserInfo userInfo;
    private final SurveyDisplayQualifier surveyDisplayQualifier;
    private final Context context;
    private final SurveysRepository surveysRepository;
    private final Executor dataExecutor;
    private final ReportManager reportManager;
    final LocalStorage localStorage;
    final RestClient restClient;
    private final Executor uiExecutor;
    private final Executor backgroundExecutor;
    private final AtomicBoolean requestingForSurvey = new AtomicBoolean(false);

    private Language preferredLanguage = new Language("en");

    private Qualaroo(Context context, Credentials credentials, boolean debugMode) {
        initLogging(debugMode);
        this.context = context.getApplicationContext();
        this.uiExecutor = new UiThreadExecutor();
        this.dataExecutor = Executors.newSingleThreadExecutor();
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
        this.localStorage = new DatabaseLocalStorage(this.context);
        this.restClient = buildRestClient(credentials);
        ApiConfig apiConfig = new ApiConfig();
        ReportClient reportClient = new ReportClient(restClient, apiConfig, localStorage);
        this.reportManager = new ReportManager(reportClient, Executors.newSingleThreadExecutor());
        SharedPreferences sharedPreferences = context.getSharedPreferences("qualaroo_prefs", Context.MODE_PRIVATE);
        Settings settings = new Settings(sharedPreferences);
        userInfo = new UserInfo(settings, localStorage);
        UserPropertiesMatcher userPropertiesMatcher = new UserPropertiesMatcher(userInfo);
        TimeMatcher timeMatcher = new TimeMatcher(new TimeProvider());
        this.surveyDisplayQualifier = new SurveyDisplayQualifier(localStorage, userPropertiesMatcher, timeMatcher);
        SessionInfo sessionInfo = new SessionInfo(this.context);
        this.surveysRepository = new SurveysRepository(credentials.siteId(), restClient, apiConfig, sessionInfo, userInfo, TimeUnit.HOURS.toMillis(1));

        QualarooLogger.info("Initialized QualarooSdk");
    }

    private void initLogging(boolean debugMode) {
        QualarooLogger.enableLogging();
        if (debugMode) {
            QualarooLogger.setDebugMode();
        }
    }

    @Override public void showSurvey(@NonNull final String alias) {
        if (alias.length() == 0) {
            throw new IllegalArgumentException("Alias can't be null or empty!");
        }
        if (requestingForSurvey.get()) {
            return;
        }
        QualarooLogger.debug("Trying to show survey: " + alias);
        requestingForSurvey.set(true);
        backgroundExecutor.execute(new Runnable() {
            @Override public void run() {
                List<Survey> surveys = surveysRepository.getSurveys();
                for (final Survey survey : surveys) {
                    if (alias.equals(survey.canonicalName())) {
                        boolean shouldShowSurvey = surveyDisplayQualifier.shouldShowSurvey(survey);
                        if (shouldShowSurvey) {
                            QualarooLogger.debug("Displaying survey " + alias);
                            uiExecutor.execute(new Runnable() {
                                @Override public void run() {
                                    QualarooActivity.showSurvey(context, survey);
                                }
                            });
                            break;
                        }
                    }
                    QualarooLogger.debug("Survey %1$s not found", alias);
                }
                requestingForSurvey.set(false);
            }
        });
    }

    @Override public void setUserId(@NonNull final String userId) {
        dataExecutor.execute(new Runnable() {
            @Override public void run() {
                userInfo.setUserId(userId);
            }
        });
    }

    @Override public void setUserProperty(@NonNull final String key, final String value) {
        dataExecutor.execute(new Runnable() {
            @Override public void run() {
                userInfo.setUserProperty(key, value);
            }
        });
    }

    @Override public void removeUserProperty(@NonNull final String key) {
        dataExecutor.execute(new Runnable() {
            @Override public void run() {
                //implicit removal of a key from local storage
                //TODO: expose removeUserProperty method
                userInfo.setUserProperty(key, null);
            }
        });
    }

    @Override public synchronized void setPreferredLanguage(@NonNull String iso2Language) {
        this.preferredLanguage = new Language(iso2Language);
    }

    SurveyComponent buildSurveyComponent(Survey survey) {
        return SurveyComponent.from(survey, localStorage, reportManager, preferredLanguage, backgroundExecutor, uiExecutor);
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

    public static class Builder implements QualarooSdk.Builder {
        private final Context context;
        private Credentials credentials;
        private boolean debugMode = false;

        Builder(Context context) {
            this.context = context;
        }

        @Override
        public Builder setApiKey(String apiKey) {
            this.credentials = new Credentials(apiKey);
            return this;
        }

        @Override
        public Builder setDebugMode(boolean debugMode) {
            this.debugMode = debugMode;
            return this;
        }

        @Override
        public void init() {
            if (INSTANCE == null) {
                INSTANCE = new Qualaroo(context, credentials, debugMode);
            }
        }
    }

}
