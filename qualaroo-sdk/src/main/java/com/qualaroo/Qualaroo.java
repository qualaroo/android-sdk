package com.qualaroo;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qualaroo.internal.AbTestGroupPercentageProvider;
import com.qualaroo.internal.ActiveStatusMatcher;
import com.qualaroo.internal.Credentials;
import com.qualaroo.internal.DeviceTypeMatcher;
import com.qualaroo.internal.ImageProvider;
import com.qualaroo.internal.InvalidCredentialsException;
import com.qualaroo.internal.SamplePercentMatcher;
import com.qualaroo.internal.SdkSession;
import com.qualaroo.internal.SurveyDisplayQualifier;
import com.qualaroo.internal.SurveyStatusMatcher;
import com.qualaroo.internal.UserGroupPercentageProvider;
import com.qualaroo.internal.UserIdentityMatcher;
import com.qualaroo.internal.UserInfo;
import com.qualaroo.internal.UserPropertiesInjector;
import com.qualaroo.internal.UserPropertiesMatcher;
import com.qualaroo.internal.executor.ExecutorSet;
import com.qualaroo.internal.executor.UiThreadExecutor;
import com.qualaroo.internal.model.Language;
import com.qualaroo.internal.model.LanguageJsonDeserializer;
import com.qualaroo.internal.model.MessageType;
import com.qualaroo.internal.model.MessageTypeDeserializer;
import com.qualaroo.internal.model.QuestionType;
import com.qualaroo.internal.model.QuestionTypeDeserializer;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.model.SurveyStatus;
import com.qualaroo.internal.network.ApiConfig;
import com.qualaroo.internal.network.Cache;
import com.qualaroo.internal.network.ImageRepository;
import com.qualaroo.internal.network.NonWorkingCache;
import com.qualaroo.internal.network.RestClient;
import com.qualaroo.internal.network.SurveysRepository;
import com.qualaroo.internal.storage.DatabaseLocalStorage;
import com.qualaroo.internal.storage.LocalStorage;
import com.qualaroo.internal.storage.Settings;
import com.qualaroo.ui.SurveyComponent;
import com.qualaroo.ui.SurveyStarter;
import com.qualaroo.util.LanguageHelper;
import com.qualaroo.util.TimeProvider;
import com.qualaroo.util.UriOpener;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
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
     *
     * @param context application {@link Context}
     * @return {@link QualarooSdk.Builder} that you can use to configure the SDK.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public static QualarooSdk.Builder initializeWith(Context context) {
        return new Builder(context);
    }

    /**
     * Returns an instance of QualarooSdk that you can use.
     * Make sure to initialize it first with:
     * {@link #initializeWith(Context) initializeWith} method calls.
     * <p>
     * Example of initialization:
     * Qualaroo.initializeWith(getApplicationContext())
     * .setApiKey("my_own_api_key")
     * .setDebugMode(false)
     * .init()
     *
     * @return current instance of {@link QualarooSdk}
     * @throws IllegalStateException when SDK was not initialized before
     */
    @SuppressWarnings("WeakerAccess")
    public static QualarooSdk getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException(
                    "Qualaroo SDK has not been properly initialized. Make sure you finish initalizeWith");
        }
        return INSTANCE;
    }

    private static void setSharedInstance(QualarooSdk qualarooSdk) {
        if (INSTANCE == null) {
            INSTANCE = qualarooSdk;
        }
    }

    private static QualarooSdk INSTANCE;

    private final LocalStorage localStorage;
    private final RestClient restClient;
    private final SurveysRepository surveysRepository;
    private final ImageProvider imageProvider;
    private final UserInfo userInfo;
    private final SurveyDisplayQualifier surveyDisplayQualifier;
    private final SurveyDisplayQualifier abTestDisplayQualifier;
    private final AbTestGroupPercentageProvider abTestGroupPercentageProvider;
    private final SurveyStarter surveyStarter;
    private final UserPropertiesInjector userPropertiesInjector;
    private final Executor dataExecutor;
    private final Executor uiExecutor;
    private final Executor backgroundExecutor;
    private final SurveyComponent.Factory surveyComponentFactory;
    private final AtomicBoolean requestingForSurvey = new AtomicBoolean(false);

    @Nullable
    private Language preferredLanguage;

    @VisibleForTesting
    Qualaroo(SurveyComponent.Factory surveyComponentFactory, SurveysRepository surveysRepository,
             SurveyStarter surveyStarter, SurveyDisplayQualifier surveyDisplayQualifier,
             SurveyDisplayQualifier abTestDisplayQualifier, UserInfo userInfo, ImageProvider imageProvider,
             RestClient restClient, LocalStorage localStorage, AbTestGroupPercentageProvider abTestGroupPercentageProvider,
             ExecutorSet executorSet, UserPropertiesInjector userPropertiesInjector) {
        this.surveyStarter = surveyStarter;
        this.surveyComponentFactory = surveyComponentFactory;
        this.abTestDisplayQualifier = abTestDisplayQualifier;
        this.restClient = restClient;
        this.localStorage = localStorage;
        this.abTestGroupPercentageProvider = abTestGroupPercentageProvider;
        this.uiExecutor = executorSet.uiThreadExecutor();
        this.dataExecutor = executorSet.dataExecutor();
        this.backgroundExecutor = executorSet.backgroundExecutor();
        this.surveysRepository = surveysRepository;
        this.userInfo = userInfo;
        this.imageProvider = imageProvider;
        this.surveyDisplayQualifier = surveyDisplayQualifier;
        this.userPropertiesInjector = userPropertiesInjector;
    }

    @NonNull
    @Override
    @WorkerThread
    public List<String> getSurveysAliases() {
        List<Survey> surveys = surveysRepository.getSurveys();
        ArrayList<String> aliases = new ArrayList<>(surveys.size());
        for (Survey survey : surveys) {
            aliases.add(survey.canonicalName());
        }
        return aliases;
    }

    @Override
    @WorkerThread
    public boolean willSurveyBeShown(@NonNull final String alias) {
        if (alias.length() == 0) {
            throw new IllegalArgumentException("Alias can't be null or empty!");
        }
        QualarooLogger.debug("Checking if survey will be shown: " + alias);
        if (requestingForSurvey.getAndSet(true)) {
            return false;
        }

        List<Survey> surveys = surveysRepository.getSurveys();
        Survey surveyToDisplay = null;
        for (final Survey survey : surveys) {
            if (alias.equals(survey.canonicalName())) {
                surveyToDisplay = survey;
                break;
            }
        }
        requestingForSurvey.set(false);
        if (surveyToDisplay != null) {
            return canDisplaySurvey(surveyToDisplay, SurveyOptions.defaultOptions(), surveyDisplayQualifier);
        } else {
            QualarooLogger.info("Survey %s not found", alias);
            return false;
        }
    }

    @Override
    public void showSurvey(@NonNull final String alias) {
        showSurvey(alias, SurveyOptions.defaultOptions());
    }

    @Override
    public void showSurvey(@NonNull final String alias, @NonNull final SurveyOptions options) {
        if (alias.length() == 0) {
            throw new IllegalArgumentException("Alias can't be null or empty!");
        }
        if (requestingForSurvey.getAndSet(true)) {
            return;
        }
        QualarooLogger.debug("Trying to show survey: " + alias);
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<Survey> surveys = surveysRepository.getSurveys();
                Survey surveyToDisplay = null;
                for (final Survey survey : surveys) {
                    if (alias.equals(survey.canonicalName())) {
                        surveyToDisplay = survey;
                        break;
                    }
                }
                if (surveyToDisplay != null) {
                    showSurvey(surveyToDisplay, options, surveyDisplayQualifier);
                } else {
                    QualarooLogger.info("Survey %s not found", alias);
                }
                requestingForSurvey.set(false);
            }
        });
    }

    private boolean showSurvey(@NonNull final Survey survey, @NonNull final SurveyOptions options, final SurveyDisplayQualifier surveyDisplayQualifier) {
        if (canDisplaySurvey(survey, options, surveyDisplayQualifier)) {
            QualarooLogger.debug("Displaying survey " + survey.canonicalName());
            Language targetLanguage = LanguageHelper.getTargetLanguage(survey, preferredLanguage);
            final Survey finalSurveyToDisplay =
                    userPropertiesInjector.injectCustomProperties(survey, targetLanguage);
            uiExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    surveyStarter.start(finalSurveyToDisplay);
                }
            });
            return true;
        }
        return false;
    }

    private boolean canDisplaySurvey(Survey survey, SurveyOptions options, SurveyDisplayQualifier surveyDisplayQualifier) {
        boolean matchesTargeting = surveyDisplayQualifier.doesQualify(survey);
        boolean canInjectProperties = userPropertiesInjector.canInjectAllProperties(survey);
        return canInjectProperties && (matchesTargeting || options.ignoreTargeting());
    }

    @Override
    public void setUserId(@NonNull final String userId) {
        dataExecutor.execute(new Runnable() {
            @Override
            public void run() {
                userInfo.setUserId(userId);
            }
        });
    }

    @Override
    public void setUserProperty(@NonNull final String key, final String value) {
        dataExecutor.execute(new Runnable() {
            @Override
            public void run() {
                userInfo.setUserProperty(key, value);
            }
        });
    }

    @Override
    public void removeUserProperty(@NonNull final String key) {
        dataExecutor.execute(new Runnable() {
            @Override
            public void run() {
                //implicit removal of a key from local storage
                //TODO: expose removeUserProperty method
                userInfo.setUserProperty(key, null);
            }
        });
    }

    @Override
    public synchronized void setPreferredLanguage(@Nullable String iso2Language) {
        if (iso2Language != null) {
            this.preferredLanguage = new Language(iso2Language);
        } else {
            this.preferredLanguage = null;
        }
    }

    @Override
    public AbTestBuilder abTest() {
        return new AbTestBuilderImpl(abTestGroupPercentageProvider);
    }

    SurveyComponent buildSurveyComponent(Survey survey) {
        return surveyComponentFactory.create(survey, preferredLanguage);
    }

    @Override
    LocalStorage localStorage() {
        return localStorage;
    }

    @Override
    RestClient restClient() {
        return restClient;
    }

    @Override
    SurveysRepository surveysRepository() {
        return surveysRepository;
    }

    @Override
    ImageProvider imageProvider() {
        return imageProvider;
    }

    private final class AbTestBuilderImpl implements AbTestBuilder {

        private final AbTestGroupPercentageProvider percentageProvider;
        private final List<String> aliases = new ArrayList<>();

        private AbTestBuilderImpl(AbTestGroupPercentageProvider percentageProvider) {
            this.percentageProvider = percentageProvider;
        }

        @Override
        public AbTestBuilder fromSurveys(List<String> aliases) {
            this.aliases.clear();
            this.aliases.addAll(aliases);
            return this;
        }

        @Override
        public void show() {
            if (aliases.isEmpty()) return;

            if (!requestingForSurvey.getAndSet(true)) {
                backgroundExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        List<Survey> abTestSurveys = getSurveys(aliases);
                        if (!abTestSurveys.isEmpty()) {
                            int percentage = percentageProvider.abTestGroupPercent(abTestSurveys);
                            Survey surveyToDisplay = findMatchingSurvey(abTestSurveys, percentage);
                            if (surveyToDisplay != null) {
                                boolean didShowSurvey = showSurvey(surveyToDisplay, SurveyOptions.defaultOptions(), abTestDisplayQualifier);
                                if (didShowSurvey) {
                                    matchSurveysAsFinished(abTestSurveys, surveyToDisplay);
                                }
                            }
                        } else {
                            QualarooLogger.debug("No surveys found for the AB test.");
                        }
                        requestingForSurvey.set(false);
                    }
                });
            }
        }

        private Survey findMatchingSurvey(List<Survey> surveys, int percentage) {
            int range = 0;
            for (Survey survey : surveys) {
                Integer samplePercent = survey.spec().requireMap().samplePercent();
                if (samplePercent == null) continue;
                if (percentage >= range && percentage < range + samplePercent) {
                    return survey;
                } else {
                    range += samplePercent;
                }
            }
            return null;
        }

        private void matchSurveysAsFinished(List<Survey> surveys, Survey exceptForSurvey) {
            for (Survey survey : surveys) {
                if (survey.equals(exceptForSurvey)) continue;
                localStorage.markSurveyFinished(survey);
            }
        }

        private List<Survey> getSurveys(List<String> aliases) {
            List<Survey> surveys = surveysRepository.getSurveys();
            List<Survey> abTestSurveys = new ArrayList<>(aliases.size());
            for (String alias : aliases) {
                for (Survey survey : surveys) {
                    if (alias.equals(survey.canonicalName())) {
                        abTestSurveys.add(survey);
                    }
                }
            }
            return abTestSurveys;
        }
    }

    public final static class Builder implements QualarooSdk.Builder {

        private static final String PREF_NAME = "qualaroo_prefs";
        private static final long NETWORK_CACHE_SIZE_IN_BYTES = 1 * 1024 * 1024;

        private final Context context;
        private String apiKey;
        private boolean debugMode = false;

        Builder(Context context) {
            this.context = context.getApplicationContext();
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
            setSharedInstance(createInstance());
            QualarooLogger.info("Initialized QualarooSdk");
            Constraints networkConstraint = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(QualarooWorker.class)
                    .setConstraints(networkConstraint)
                    .build();
            WorkManager.getInstance(context).enqueue(workRequest);
        }

        @SuppressWarnings("WeakerAccess")
        public QualarooSdk createInstance() {
            try {
                if (debugMode) {
                    QualarooLogger.setDebugMode();
                }
                Credentials credentials = new Credentials(apiKey);
                OkHttpClient okHttpClient = buildOkHttpClient();
                RestClient restClient = buildRestClient(okHttpClient, credentials);
                LocalStorage localStorage = new DatabaseLocalStorage(context);
                SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                Settings settings = new Settings(sharedPreferences);
                UserInfo userInfo = new UserInfo(settings, localStorage);
                ExecutorSet executorSet = new ExecutorSet(new UiThreadExecutor(), Executors.newSingleThreadExecutor(), Executors.newSingleThreadExecutor());
                UriOpener uriOpener = new UriOpener(context);
                ImageRepository imageRepository = new ImageRepository(okHttpClient, context.getCacheDir());
                ImageProvider imageProvider = new ImageProvider(context, imageRepository, executorSet.backgroundExecutor(), executorSet.uiThreadExecutor());
                SurveyComponent.Factory componentFactory = new SurveyComponent.Factory(context, restClient, localStorage, userInfo, executorSet, uriOpener, imageProvider);
                SdkSession sdkSession = new SdkSession(this.context, new DeviceTypeMatcher.AndroidDeviceTypeProvider(context));
                SurveyStarter surveyStarter = new SurveyStarter(context);
                TimeProvider timeProvider = new TimeProvider() {
                    @Override
                    public long currentTimeMillis() {
                        return System.currentTimeMillis();
                    }
                };
                Cache<List<Survey>> cache = BuildConfig.DEBUG ?
                        new NonWorkingCache<List<Survey>>() :
                        new Cache<List<Survey>>(timeProvider, TimeUnit.HOURS.toMillis(1));
                ApiConfig apiConfig = new ApiConfig();
                SurveysRepository surveysRepository = new SurveysRepository(credentials.siteId(), restClient, apiConfig, sdkSession, userInfo, cache);

                UserPropertiesInjector userPropertiesInjector = new UserPropertiesInjector(userInfo);
                SurveyDisplayQualifier surveyDisplayQualifier = SurveyDisplayQualifier.builder()
                        .register(new ActiveStatusMatcher())
                        .register(new SurveyStatusMatcher(localStorage))
                        .register(new UserPropertiesMatcher(userInfo))
                        .register(new UserIdentityMatcher(userInfo))
                        .register(new DeviceTypeMatcher(new DeviceTypeMatcher.AndroidDeviceTypeProvider(this.context)))
                        .register(new SamplePercentMatcher(new UserGroupPercentageProvider(localStorage, new SecureRandom())))
                        .build();

                SurveyDisplayQualifier abTestDisplayQualifier = SurveyDisplayQualifier.builder()
                        .register(new ActiveStatusMatcher())
                        .register(new SurveyStatusMatcher(localStorage))
                        .register(new UserPropertiesMatcher(userInfo))
                        .register(new UserIdentityMatcher(userInfo))
                        .register(new DeviceTypeMatcher(new DeviceTypeMatcher.AndroidDeviceTypeProvider(this.context)))
                        .build();

                AbTestGroupPercentageProvider abTestGroupPercentageProvider = new AbTestGroupPercentageProvider(localStorage, new SecureRandom());
                return new Qualaroo(componentFactory, surveysRepository, surveyStarter, surveyDisplayQualifier,
                        abTestDisplayQualifier, userInfo, imageProvider, restClient, localStorage, abTestGroupPercentageProvider, executorSet,
                        userPropertiesInjector);
            } catch (InvalidCredentialsException e) {
                return new InvalidApiKeyQualarooSdk(apiKey);
            } catch (Exception e) {
                //TODO: this is unexpected and might be an OS bug, log this event in our own company's bug tracker
                return new InvalidApiKeyQualarooSdk(apiKey);
            }
        }

        private OkHttpClient buildOkHttpClient() {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            if (BuildConfig.DEBUG) {
                final HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                    @Override
                    public void log(String message) {
                        QualarooLogger.info(message);
                    }
                });
                httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                builder.addInterceptor(httpLoggingInterceptor);
            }
            return builder.build();
        }

        private RestClient buildRestClient(OkHttpClient okHttpClient, Credentials credentials) {
            final String authToken = okhttp3.Credentials.basic(credentials.apiKey(), credentials.apiSecret());
            OkHttpClient.Builder builder = okHttpClient.newBuilder();
            builder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request().newBuilder()
                            .header("Authorization", authToken)
                            .build();
                    return chain.proceed(request);
                }
            });
            builder.cache(new okhttp3.Cache(context.getCacheDir(), NETWORK_CACHE_SIZE_IN_BYTES));
            okHttpClient = builder.build();

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Language.class, new LanguageJsonDeserializer())
                    .registerTypeAdapter(QuestionType.class, new QuestionTypeDeserializer())
                    .registerTypeAdapter(MessageType.class, new MessageTypeDeserializer())
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();

            return new RestClient(okHttpClient, gson);
        }

    }

    private static class InvalidApiKeyQualarooSdk implements QualarooSdk {

        private final String providedApiKey;

        InvalidApiKeyQualarooSdk(String providedApiKey) {
            this.providedApiKey = providedApiKey;
            logErrorMessage();
        }

        @NonNull
        @Override
        public List<String> getSurveysAliases() {
            logErrorMessage();
            return new ArrayList<>();
        }

        @Override
        public boolean willSurveyBeShown(@NonNull String alias) {
            logErrorMessage();
            return false;
        }

        @Override
        public void showSurvey(@NonNull String alias) {
            logErrorMessage();
        }

        @Override
        public void showSurvey(@NonNull String alias, @NonNull SurveyOptions options) {
            logErrorMessage();
        }

        @Override
        public void setUserId(@NonNull String userId) {
            logErrorMessage();
        }

        @Override
        public void setUserProperty(@NonNull String key, @Nullable String value) {
            logErrorMessage();
        }

        @Override
        public void removeUserProperty(@NonNull String key) {
            logErrorMessage();
        }

        @Override
        public void setPreferredLanguage(@Nullable String iso2Language) {
            logErrorMessage();
        }

        @Override
        public AbTestBuilder abTest() {
            return new AbTestBuilderStub();
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

        private class AbTestBuilderStub implements AbTestBuilder {

            @Override
            public AbTestBuilder fromSurveys(List<String> aliases) {
                return this;
            }

            @Override
            public void show() {
                logErrorMessage();
            }
        }
    }

}
