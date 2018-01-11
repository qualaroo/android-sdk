package com.qualaroo;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qualaroo.internal.Credentials;
import com.qualaroo.internal.DeviceTypeMatcher;
import com.qualaroo.internal.ImageProvider;
import com.qualaroo.internal.InvalidCredentialsException;
import com.qualaroo.internal.ReportManager;
import com.qualaroo.internal.SamplePercentMatcher;
import com.qualaroo.internal.SessionInfo;
import com.qualaroo.internal.SurveyDisplayQualifier;
import com.qualaroo.internal.UserGroupPercentageProvider;
import com.qualaroo.internal.UserIdentityMatcher;
import com.qualaroo.internal.UserInfo;
import com.qualaroo.internal.UserPropertiesMatcher;
import com.qualaroo.internal.executor.UiThreadExecutor;
import com.qualaroo.internal.model.Language;
import com.qualaroo.internal.model.LanguageJsonDeserializer;
import com.qualaroo.internal.model.MessageType;
import com.qualaroo.internal.model.MessageTypeDeserializer;
import com.qualaroo.internal.model.QuestionType;
import com.qualaroo.internal.model.QuestionTypeDeserializer;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.network.ApiConfig;
import com.qualaroo.internal.network.Cache;
import com.qualaroo.internal.network.ImageRepository;
import com.qualaroo.internal.network.ReportClient;
import com.qualaroo.internal.network.RestClient;
import com.qualaroo.internal.network.SurveysRepository;
import com.qualaroo.internal.storage.DatabaseLocalStorage;
import com.qualaroo.internal.storage.LocalStorage;
import com.qualaroo.internal.storage.Settings;
import com.qualaroo.ui.SurveyComponent;
import com.qualaroo.util.Shuffler;
import com.qualaroo.util.TimeProvider;
import com.qualaroo.util.UriOpener;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public final class Qualaroo extends QualarooBase implements QualarooSdk {

    /**
     * Starts initialization phase of the SDK.
     * Make sure to call {@link QualarooSdk.Builder#init()} to finish initialization properly.
     * @param context application {@link Context}
     * @return {@link QualarooSdk.Builder} that you can use to configure the SDK.
     */
    @SuppressWarnings({"WeakerAccess", "unused"}) public static Builder initializeWith(Context context) {
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
    @SuppressWarnings("WeakerAccess") public static QualarooSdk getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException(
                    "Qualaroo SDK has not been properly initialized. Make sure you finish initalizeWith");
        }
        return INSTANCE;
    }

    private static QualarooSdk INSTANCE;

    private final LocalStorage localStorage;
    private final RestClient restClient;
    private final SurveysRepository surveysRepository;
    private final ImageProvider imageProvider;

    private final UserInfo userInfo;
    private final SurveyDisplayQualifier surveyDisplayQualifier;
    private final Context context;
    private final Executor dataExecutor;
    private final ReportManager reportManager;
    private final UriOpener uriOpener;
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
        OkHttpClient okHttpClient = buildOkHttpClient();
        this.restClient = buildRestClient(okHttpClient, credentials);
        ImageRepository imageRepository = new ImageRepository(okHttpClient, context.getCacheDir());
        this.imageProvider = new ImageProvider(context, imageRepository, backgroundExecutor, uiExecutor);
        this.uriOpener = new UriOpener(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences("qualaroo_prefs", Context.MODE_PRIVATE);
        Settings settings = new Settings(sharedPreferences);
        this.userInfo = new UserInfo(settings, localStorage);
        ApiConfig apiConfig = new ApiConfig();
        ReportClient reportClient = new ReportClient(restClient, apiConfig, localStorage, userInfo);
        this.reportManager = new ReportManager(reportClient, Executors.newSingleThreadExecutor());

        this.surveyDisplayQualifier = SurveyDisplayQualifier.builder()
                .register(new UserPropertiesMatcher(userInfo))
                .register(new UserIdentityMatcher(userInfo))
                .register(new DeviceTypeMatcher(new DeviceTypeMatcher.AndroidDeviceTypeProvider(this.context)))
                .register(new SamplePercentMatcher(new UserGroupPercentageProvider(localStorage, new SecureRandom())))
                .build();

        SessionInfo sessionInfo = new SessionInfo(this.context);

        Cache<List<Survey>> cache = BuildConfig.DEBUG ?
                new NonWorkingCache<List<Survey>>() :
                new Cache<List<Survey>>(TimeProvider.DEFAULT, TimeUnit.HOURS.toMillis(1));

        this.surveysRepository = new SurveysRepository(credentials.siteId(), restClient, apiConfig, sessionInfo, userInfo, cache);

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
                Survey surveyToDisplay = null;
                for (final Survey survey : surveys) {
                    if (alias.equals(survey.canonicalName())) {
                        surveyToDisplay = survey;
                        break;
                    }
                }
                if (surveyToDisplay != null) {
                    boolean shouldShowSurvey = surveyDisplayQualifier.shouldShowSurvey(surveyToDisplay);
                    if (shouldShowSurvey) {
                        QualarooLogger.debug("Displaying survey " + alias);
                        final Survey finalSurveyToDisplay = surveyToDisplay;
                        uiExecutor.execute(new Runnable() {
                            @Override public void run() {
                                QualarooActivity.showSurvey(context, finalSurveyToDisplay);
                            }
                        });
                    }
                } else {
                    QualarooLogger.info("Survey %s not found", alias);
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
        return SurveyComponent.from(survey, localStorage, reportManager, preferredLanguage, new Shuffler(), backgroundExecutor, uiExecutor, uriOpener, imageProvider);
    }

    private RestClient buildRestClient(OkHttpClient okHttpClient, Credentials credentials) {
        final String authToken = okhttp3.Credentials.basic(credentials.apiKey(), credentials.apiSecret());
        OkHttpClient.Builder builder = okHttpClient.newBuilder();
        builder.addInterceptor(new Interceptor() {
                    @Override public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request().newBuilder()
                                .header("Authorization", authToken)
                                .build();
                        return chain.proceed(request);
                    }
                });
        okHttpClient = builder.build();
        return new RestClient(okHttpClient, buildGson());
    }

    private OkHttpClient buildOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            final HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override public void log(String message) {
                    QualarooLogger.info(message);
                }
            });
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(httpLoggingInterceptor);
        }
        return builder.build();
    }

    private Gson buildGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Language.class, new LanguageJsonDeserializer())
                .registerTypeAdapter(QuestionType.class, new QuestionTypeDeserializer())
                .registerTypeAdapter(MessageType.class, new MessageTypeDeserializer())
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    @Override LocalStorage localStorage() {
        return localStorage;
    }

    @Override RestClient restClient() {
        return restClient;
    }

    @Override SurveysRepository surveysRepository() {
        return surveysRepository;
    }

    @Override ImageProvider imageProvider() {
        return imageProvider;
    }

    public final static class Builder implements QualarooSdk.Builder {
        private final Context context;
        private String apiKey;
        private boolean debugMode = false;

        Builder(Context context) {
            this.context = context;
            //TODO: figure out a way of avoiding calling enableLogging() method everywhere
            //(this method has been exposed to avoid using Log.d calls in unit tests)
            QualarooLogger.enableLogging();
        }

        @Override
        public QualarooSdk.Builder setApiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        @Override
        public QualarooSdk.Builder setDebugMode(boolean debugMode) {
            this.debugMode = debugMode;
            return this;
        }

        @Override
        public void init() {
            if (INSTANCE != null) {
                return;
            }
            try {
                Credentials credentials = new Credentials(apiKey);
                INSTANCE = new Qualaroo(context, credentials, debugMode);
                QualarooJobIntentService.start(context);
            } catch (InvalidCredentialsException e) {
                INSTANCE = new InvalidApiKeyQualarooSdk(apiKey);
            } catch (Exception e) {
                //TODO: this is unexpected and might be an OS bug, log this event in our own company's bug tracker
                INSTANCE = new InvalidApiKeyQualarooSdk(apiKey);
            }
        }
    }

    private static class NonWorkingCache<T> extends Cache<T> {
        NonWorkingCache() {
            super(TimeProvider.DEFAULT, 0);
        }

        @Override public boolean isStale() {
            return true;
        }

        @Override public boolean isInvalid() {
            return true;
        }
    }

    private static class InvalidApiKeyQualarooSdk implements QualarooSdk {

        private final String providedApiKey;

        InvalidApiKeyQualarooSdk(String providedApiKey) {
            this.providedApiKey = providedApiKey;
            QualarooLogger.enableLogging();
            logErrorMessage();
        }

        @Override public void showSurvey(@NonNull String alias) {
            logErrorMessage();
        }

        @Override public void setUserId(@NonNull String userId) {
            logErrorMessage();
        }

        @Override public void setUserProperty(@NonNull String key, @Nullable String value) {
            logErrorMessage();
        }

        @Override public void removeUserProperty(@NonNull String key) {
            logErrorMessage();
        }

        @Override public void setPreferredLanguage(@NonNull String iso2Language) {
            logErrorMessage();
        }

        private void logErrorMessage() {
            QualarooLogger.error(
                    String.format(
                            Locale.ROOT,
                            "Qualaroo SDK has not been properly initialized. Key: %1$s seems to be an incorrect one.",
                            providedApiKey
                    )
            );
        }
    }

}
