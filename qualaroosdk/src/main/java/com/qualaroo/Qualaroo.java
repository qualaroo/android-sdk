/*
 * Copyright © 2017 Qualaroo. All rights reserved.
 */

package com.qualaroo;
/*
 * Created by Artem Orynko on 12.04.17.
 */

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.qualaroo.helpers.Logger;
import com.qualaroo.helpers.NetworkChangeReceiver;
import com.qualaroo.helpers.NetworkChangeReceiver.NetworkChangeReceiverListener;
import com.qualaroo.helpers.SurveyManager;
import com.qualaroo.views.QualarooBackgroundView;
import com.qualaroo.views.QualarooBackgroundView.QualarooBackgroundViewDelegate;
import com.qualaroo.views.QualarooWebView;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import static android.view.Gravity.BOTTOM;
import static android.view.Gravity.CENTER_HORIZONTAL;
import static android.view.Gravity.END;
import static android.view.Gravity.START;
import static android.view.Gravity.TOP;
import static com.qualaroo.helpers.SurveyManager.SURVEY_ANSWERED;
import static com.qualaroo.helpers.SurveyManager.SURVEY_SHOWN;
import static com.qualaroo.helpers.SurveyManager.isAliasExists;
import static com.qualaroo.helpers.SurveyManager.isShowingSurvey;
import static com.qualaroo.helpers.Utils.getDeviceId;
import static com.qualaroo.helpers.Utils.getResourceString;
import static com.qualaroo.helpers.Utils.getScriptURL;
import static com.qualaroo.helpers.Utils.hasPermission;
import static com.qualaroo.helpers.Utils.isNullOrEmpty;
import static com.qualaroo.helpers.Utils.isTablet;
import static com.qualaroo.helpers.Utils.validAPIKey;

public class Qualaroo extends WebViewClient implements QualarooBackgroundViewDelegate, NetworkChangeReceiverListener {

    private static final String QUALAROO_API_KEY_RESOURCE_IDENTIFIER = "qualaroo_api_key";
    private static final String BASE_URL = "file:///android_asset/qualaroo_host.html";

    private static volatile Qualaroo singleton = null;

    private final Context context;
    private final String apiKey;
    private final Logger logger;
    private String identifier;
    private Position position;
    private int backgroundColor;
    private String customStyleURL;

    private QualarooWebView webView;
    private QualarooBackgroundView backgroundView;
    private Activity hostingActivity;

    private NetworkChangeReceiver networkChangeReceiver;
    private Semaphore isSurveyInformationSavedSemaphore = new Semaphore(1);
    private Semaphore surveyIsShowingSemaphore = new Semaphore(1);

    private float suggestedHeight;
    private Position localPosition = null;
    private Number localBackgroundColor = null;
    private boolean isLoadedScript;

    private ArrayList<String> unfulfilledReportRequests;

    /**
     * Return a reference to the global default {@link Qualaroo} instance.
     *
     * <p>This instance is automatically initialized with defaults that are suitable to most
     * implementations.
     *
     * <p>If these settings do not meet the requirements of your application, you can override
     * defaults in {@code qualaroo.xml}, or you can construct your own instance with full control
     * over the configuration by using {@link Builder}.
     *
     * <p>By default debugging is disabled.
     */
    public static Qualaroo with(Context context) {
        if (singleton == null) {
            if (context == null) {
                throw new IllegalArgumentException("Context must not be null.");
            }
            synchronized (Qualaroo.class) {
                if (singleton == null) {
                    String apiKey = getResourceString(context, QUALAROO_API_KEY_RESOURCE_IDENTIFIER);
                    Builder builder = new Builder(context, apiKey);

                    try {
                        String packageName = context.getPackageName();
                        int flags = context.getPackageManager().getApplicationInfo(packageName, 0).flags;
                        boolean debugging = (flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
                        if (debugging) {
                            builder.logLevel(LogLevel.INFO);
                        }
                    }catch (PackageManager.NameNotFoundException ignored) {}

                    singleton = builder.build();
                }
            }
        }
        return singleton;
    }

    /**
     * Set the global instance returned from {@link #with}.
     *
     * <p>This method must be called before any calls to {@link #with} and may only be called once.
     */
    public static void setSingletonInstance(Qualaroo qualaroo) {
        synchronized (Qualaroo.class) {
            if (singleton != null) {
                throw new IllegalArgumentException("Singleton instance already exists.");
            }
            singleton = qualaroo;
        }
    }

    private Qualaroo(
            Context context,
            String apiKey,
            Logger logger,
            final boolean autoTrackScreen,
            Position position,
            int backgroundColor,
            String customStyleURL
    ) {
        this.context = context;
        this.apiKey = apiKey;
        this.logger = logger;
        this.position = position;
        this.backgroundColor = backgroundColor;
        this.customStyleURL = customStyleURL;

        try {
            isSurveyInformationSavedSemaphore.acquire();
        } catch (InterruptedException e) {
            logger.error(e, e.getLocalizedMessage());
        }

        networkChangeReceiver = new NetworkChangeReceiver();
        NetworkChangeReceiver.delegate = this;

        Application application = (Application) context.getApplicationContext();
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                activity.registerReceiver(networkChangeReceiver, intentFilter);
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                if (autoTrackScreen) {
                    attachToActivity(activity);
                    String activityName = getActivityLabel(activity).replace("Activity", "");
                    showSurvey(activityName, true);
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                if (autoTrackScreen) {
                    removeFromActivity();
                }
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                activity.unregisterReceiver(networkChangeReceiver);
            }
        });
        setupWebView();
        setupBackgroundView();

        logger.debug("Created Qualaroo client for project with API Key: %s", apiKey);
    }


    /**
     * Attaches Qualaroo's survey view to a given activity
     * @param activity The view activity that will be used to host surveys.
     * @return return true if Qualaroo's survey is attached.
     */
    public boolean attachToActivity(Activity activity) {
        return attachToActivity(activity, getPosition());
    }

    /**
     * Attaches Qualaroo's survey view to a given view Activity
     * @param activity The view activity that will be used to host surveys.
     * @param position The attachment position only for given activity
     * @return Returns true if Qualaroo's survey is attached.
     */
    public boolean attachToActivity(Activity activity, Position position) {
        if (activity == null) {
            throw new IllegalArgumentException("Activity must not be null.");
        }
        this.hostingActivity = activity;
        this.localPosition = position;

        Position positionForSurvey = getPosition();
        if (!isTablet(context)) {
            if (positionForSurvey != Position.TOP
                    && positionForSurvey != Position.BOTTOM) {
                throw new IllegalArgumentException("Supported position on this platform are TOP or BOTTOM.");
            }
        }

        backgroundView.setGravity(getGravityForPosition(positionForSurvey));
        backgroundView.setBackgroundColor(getBackgroundColor());
        hostingActivity.addContentView(backgroundView, backgroundView.getLayoutParams());

        return true;
    }

    /** Returns true if the survey is removed from the activity. */
    public boolean removeFromActivity() {

        boolean wasAttachedToActivity;
        ViewParent viewParent;

        viewParent = backgroundView.getParent();
        wasAttachedToActivity = viewParent != null;

        if (webView.getVisibility() == View.VISIBLE) {
            closeSurvey();
        }

        if (wasAttachedToActivity) {
            ((ViewGroup) viewParent).removeView(backgroundView);
        }
        localPosition = null;
        localBackgroundColor = null;

        return wasAttachedToActivity;
    }

    /**
     * Display survey with given alias on a current activity.
     * @param alias The survey alias to display.
     */
    public void showSurvey(String alias) {
        showSurvey(alias, true);
    }

    /**
     * Display survey with given alias on a current activity.
     * @param alias The survey alias to display.
     * @param shouldForce Force a survey to show overriding target settings.
     */
    public void showSurvey(final String alias, final boolean shouldForce) {
        if (hostingActivity == null) {
            throw new IllegalArgumentException("Activity must not be null.");
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String aliasToShow = null;
                try {
                    isSurveyInformationSavedSemaphore.acquire();
                } catch (InterruptedException e) {
                    logger.error(e, e.getLocalizedMessage());
                } finally {
                    isSurveyInformationSavedSemaphore.release();
                    aliasToShow = isAliasExists(context, alias);
                }

                if (isNullOrEmpty(aliasToShow)) {
                    logger.info("The given alias '%s' doesn't exist.", aliasToShow);
                    return;
                }
                boolean showSurvey = isShowingSurvey(context, aliasToShow, identifier, logger);
                if (showSurvey) {
                    try {
                        surveyIsShowingSemaphore.acquire();
                    } catch (InterruptedException e) {
                        logger.error(e, e.getLocalizedMessage());
                    } finally {
                        surveyIsShowingSemaphore.release();
                        triggerSurvey(aliasToShow, shouldForce);
                    }
                }
            }
        }).start();
    }

    /**
     * Identifier Code based on the string.
     * @param identifier Unique User ID.
     */
    public void identify(String identifier) {
        if (isNullOrEmpty(identifier)) {
            throw new IllegalArgumentException("Identifier must not be null or empty.");
        }
        this.identifier = identifier;
    }

    /**
     * Set the position for surveys on the current activity.
     * @param position See {@link Position}
     */
    public void setPosition(Position position) {
        if (position == null) {
            throw new IllegalArgumentException("Position must not be null.");
        }
        this.localPosition = position;
    }

    /**
     * Set color for background on the current activity.
     * Working only for Phones.
     * @param color See {@link Color}
     */
    public void setBackgroundColor(int color) {
        this.localBackgroundColor = color;
    }

    private void setupWebView() {

        webView = new QualarooWebView(context);
        webView.setWebViewClient(this);
        webView.addJavascriptInterface(new JavaScriptInterface(), "Qualaroo");

        // Load HTML
        webView.loadUrl(BASE_URL);
    }
    private void setupBackgroundView() {

        backgroundView = new QualarooBackgroundView(context);
        backgroundView.delegate = this;

        if (isTablet(context)) {
            backgroundView.setBackgroundColor(Color.TRANSPARENT);
        }
        backgroundView.addView(webView);
    }

    private String getIdentifier() {
        if (isNullOrEmpty(identifier)) {
            identifier = getDeviceId(context);
        }
        return identifier;
    }
    private int getGravityForPosition(Position position) {
        int result = 0;

        switch (position) {
            case TOP:
                if (isTablet(context)) {
                    result = TOP|CENTER_HORIZONTAL;
                } else {
                    result = TOP;
                }
                break;
            case BOTTOM:
                if (isTablet(context)) {
                    result = BOTTOM|CENTER_HORIZONTAL;
                } else {
                    result = BOTTOM;
                }
                break;
            case TOP_LEFT:
                result = TOP|START;
                break;
            case TOP_RIGHT:
                result = TOP|END;
                break;
            case BOTTOM_LEFT:
                result = BOTTOM|START;
                break;
            case BOTTOM_RIGHT:
                result = BOTTOM|END;
                break;
        }
        return result;
    }

    private Position getPosition() {
        if (localPosition == null) {
            return position;
        } else {
            return localPosition;
        }
    }

    private int getBackgroundColor() {
        if (isTablet(context)) {
            return Color.TRANSPARENT;
        }
        if (localBackgroundColor == null) {
            return backgroundColor;
        } else {
            return (int) localBackgroundColor;
        }
    }

    private String getActivityLabel(Activity activity) {
        String activityLabel = null;
        PackageManager packageManager = activity.getPackageManager();
        try {
            ActivityInfo activityInfo = packageManager.getActivityInfo(
                    activity.getComponentName(),
                    PackageManager.GET_META_DATA
            );
            activityLabel = activityInfo.loadLabel(packageManager).toString();
        } catch (PackageManager.NameNotFoundException e) {
            throw new AssertionError("Activity Not Found: " + e.toString());
        }
        return activityLabel;
    }

    private void triggerSurvey(String alias, boolean shouldForce) {
        final String jsScript = "triggerSurvey('" + alias + "', " + shouldForce + ");";

        hostingActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavaScript(jsScript, null);
            }
        });
    }

    private void closeSurvey() {
        String jsScript = "_kiq.push(['stopSurvey']);";
        webView.evaluateJavaScript(jsScript, null);
    }

    private void setIdentityCode() {

        String identifier = getIdentifier();

        logger.debug("Qualaroo Identity Code set to %s", identifier);
        String jsScript = "_kiq.push(['identify', '" + identifier + "']);";
        webView.evaluateJavaScript(jsScript, null);
    }

    private void checkAndroidVersion() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            String jsScript = "isOldVersion = true;";
            webView.evaluateJavaScript(jsScript, null);
        }
    }
    private void loadCustomStyle() {
        if (!isNullOrEmpty(customStyleURL)) {
            String jsString = "qualarooHost.loadStylesheetIfNotAlreadyLoaded('" + customStyleURL + "', function() {});";
            webView.evaluateJavaScript(jsString, null);
        }
    }

    private void loadQualarooScript() {
        String jsScript = "loadQualarooScriptIfNeeded('" + getScriptURL(apiKey) + "');";
        webView.evaluateJavaScript(jsScript, null);
    }

    private void setSuggestedHeight(float suggestedHeight) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();

        display.getSize(size);

        int newSuggestedHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                suggestedHeight,
                context.getResources().getDisplayMetrics()
        );
        int halfScreenHeight = size.y / 2;

        if (halfScreenHeight < newSuggestedHeight) {
            this.suggestedHeight = halfScreenHeight;
        } else {
            this.suggestedHeight = newSuggestedHeight;
        }
    }

    private void updateHeight() {
        int newSuggestedHeight = (int) suggestedHeight;
        final LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) webView.getLayoutParams();

        layoutParams.height = newSuggestedHeight;

        if (isTablet(context)) {
            layoutParams.width = context.getResources().getDisplayMetrics().widthPixels / 2;
        }
        hostingActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.setLayoutParams(layoutParams);
            }
        });
    }

    private void performShow() {
        final TranslateAnimation translateAnimation = showAnimation();

        translateAnimation.setDuration(500);
        translateAnimation.setFillAfter(true);

        hostingActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                backgroundAnimation(0.2f, 0.9f);
                webView.setVisibility(View.VISIBLE);
                webView.startAnimation(translateAnimation);
            }
        });
    }

    private void performHide() {
        final TranslateAnimation translateAnimation = hideAnimation();

        translateAnimation.setDuration(50);
        translateAnimation.setFillAfter(true);

        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                backgroundAnimation(0.9f, 0.0f);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                backgroundView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        hostingActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.setVisibility(View.GONE);
                webView.setAnimation(translateAnimation);
            }
        });
    }

    private TranslateAnimation showAnimation() {
        int height = webView.getHeight();
        int width = webView.getWidth();

        switch (getPosition()) {
            case TOP:
                return new TranslateAnimation(0, 0, -height, 0);
            case BOTTOM:
                return new TranslateAnimation(0, 0, height, 0);
            case TOP_LEFT:
                return new TranslateAnimation(-width, 0, 0, 0);
            case TOP_RIGHT:
                return new TranslateAnimation(width, 0, 0, 0);
            case BOTTOM_LEFT:
                return new TranslateAnimation(-width, 0, 0, 0);
            case BOTTOM_RIGHT:
                return new TranslateAnimation(width, 0, 0, 0);
            default:
                return new TranslateAnimation(0, 0, 0, 0);
        }
    }

    private TranslateAnimation hideAnimation() {

        int height = webView.getHeight();
        int width = webView.getWidth();

        switch (getPosition()) {
            case TOP:
                return new TranslateAnimation(0, 0, 0, -height);
            case BOTTOM:
                return new TranslateAnimation(0, 0, 0, height);
            case TOP_LEFT:
                return new TranslateAnimation(0, -width, 0, 0);
            case TOP_RIGHT:
                return new TranslateAnimation(0, width, 0, 0);
            case BOTTOM_LEFT:
                return new TranslateAnimation(0, -width, 0, 0);
            case BOTTOM_RIGHT:
                return new TranslateAnimation(0, width, 0, 0);
            default:
                return new TranslateAnimation(0, 0, 0, 0);
        }
    }

    private void backgroundAnimation(float start, float end) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(start, end);

        alphaAnimation.setDuration(500);
        alphaAnimation.setFillAfter(false);

        backgroundView.setVisibility(View.VISIBLE);
        backgroundView.startAnimation(alphaAnimation);
    }

    private void setInteraction(View view, final boolean success) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return success;
            }
        });
    }

    private ArrayList<String> getUnfulfilledReportRequests() {
        if (unfulfilledReportRequests == null) {
            unfulfilledReportRequests = new ArrayList<>();
        }
        return unfulfilledReportRequests;
    }

    private void errorSendingReportRequest(String url) {
        logger.debug("Adding URL %s to unfulfilled report requests list.", url);

        getUnfulfilledReportRequests().add(url);
        if (NetworkChangeReceiver.isConnected(context)) {
            attemptToDeliveryUnfulfilledReportRequests();
        }
    }

    private void attemptToDeliveryUnfulfilledReportRequests() {

        final ArrayList<String> markedForRemoval = unfulfilledReportRequests;
        RequestQueue queue = Volley.newRequestQueue(context);

        for (int i = 0; i < markedForRemoval.size(); i++) {
            final String url = markedForRemoval.get(i);

            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            logger.debug("Successfully sent unfulfilled request with URL: %s", url);
                            unfulfilledReportRequests.remove(url);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            logger.debug("Unable to send unfulfilled request with URL: %s", url);
                        }
            });
            queue.add(stringRequest);
        }
    }

    //region QualarooBackgroundViewDelegate
    @Override
    public void handleTap() {
        closeSurvey();
    }
    //endregion

    //region JavaScriptInterface
    private class JavaScriptInterface {
        @JavascriptInterface
        public void qualarooScriptLoadSuccess(String message) {
            logger.debug("Load script: %s", message);

            isLoadedScript = true;
            setIdentityCode();
            checkAndroidVersion();
            loadCustomStyle();
        }
        @JavascriptInterface
        public void saveSurveysInfo(String message) {
            logger.info("Saving of surveys information.");

            final String surveysInformation = message;

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void ...params) {
                    SurveyManager.saveSurveysInformation(context, surveysInformation);
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);
                    logger.info("Information about surveys saved.");
                    isSurveyInformationSavedSemaphore.release();
                }
            }.execute();
        }
        @JavascriptInterface
        public void surveyHeightChanged(float suggestedHeight) {
            logger.info("Survey height changed. New height suggested: %f", suggestedHeight);

            setSuggestedHeight(suggestedHeight);
            updateHeight();
        }
        @JavascriptInterface
        public void surveyScreenerReady() {
            try {
                surveyIsShowingSemaphore.acquire();
            } catch (InterruptedException e) {
                logger.error(e, e.getLocalizedMessage());
            } finally {
                logger.info("Screen is showing.");
                performShow();
            }
        }
        @JavascriptInterface
        public void surveyShow() {
            try {
                surveyIsShowingSemaphore.acquire();
            } catch (InterruptedException e) {
                logger.error(e, e.getLocalizedMessage());
            } finally {
                logger.info("Survey is showing.");
                performShow();
            }
        }
        @JavascriptInterface
        public void surveyClosed(String message) {
            logger.info("Survey closed");

            if (webView.getVisibility() == View.VISIBLE) {
                logger.info("Hiding survey");

                performHide();

                SurveyManager.addInformation(context, identifier, SURVEY_SHOWN, message);

                surveyIsShowingSemaphore.release();
            }
        }
        @JavascriptInterface
        public void qualarooStartScroll() {
            logger.info("Begin ignoring interaction.");
            setInteraction(webView, true);
            setInteraction(backgroundView, true);
        }
        @JavascriptInterface
        public void qualarooStopScroll() {
            logger.info("End ignoring interaction.");
            setInteraction(webView, false);
            setInteraction(backgroundView, false);
        }
        @JavascriptInterface
        public void answeredSurvey(String message) {
            logger.info("The customer %s answered the survey %s.", identifier, message);

            SurveyManager.addInformation(context, identifier, SURVEY_ANSWERED, message);
        }
        @JavascriptInterface
        public void qualarooScriptLoadError(String message) {
            logger.info("Failed to load script: %s", message);
            isLoadedScript = false;
        }
        @JavascriptInterface
        public void surveyUndeliveredAnswerRequest(String message) {
            logger.info("Undelivered answer request: %s", message);

            errorSendingReportRequest(message);
        }
        @JavascriptInterface
        public void reloadQualarooData() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        logger.info("Waiting for the closing of the survey.");
                        surveyIsShowingSemaphore.acquire();
                    } catch (InterruptedException e) {
                        logger.error(e, e.getLocalizedMessage());
                    } finally {
                        logger.info("Reloading data from a server.");
                        surveyIsShowingSemaphore.release();
                        try {
                            isSurveyInformationSavedSemaphore.acquire();
                        } catch (InterruptedException e) {
                            logger.error(e, e.getLocalizedMessage());
                        } finally {
                            isSurveyInformationSavedSemaphore.release();
                            hostingActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    webView.loadUrl(BASE_URL);
                                }
                            });
                            logger.info("Tha data was reloaded.");
                        }
                    }
                }
            }).start();
        }
        @JavascriptInterface
        public void globalUnhandledJSError(String message) {
            logger.debug(message);
        }
    }

    //endregion

    //region Builder
    /** Fluent API for creating {@link Qualaroo} instance. */
    @Keep
    public static class Builder {

        private final Context context;
        private final Application application;
        private String apiKey;
        private LogLevel logLevel;
        private boolean autoTrackScreen = false;
        private Position position = Position.BOTTOM;
        private int backgroundColor = Color.argb(128, 128, 128, 128);
        private String customStyleURL = null;

        /** Start building a new {@link Qualaroo} instance. */
        public Builder(Context context, String apiKey) {
            if (context == null) {
                throw new IllegalArgumentException("Context must not be null.");
            }
            this.context = context;

            if (!hasPermission(context, Manifest.permission.INTERNET)) {
                throw new IllegalArgumentException("INTERNET permission is required.");
            }
            if (!hasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
                throw new IllegalArgumentException("ACCESS_NETWORK_STATE permission is required");
            }

            this.application = (Application) context.getApplicationContext();
            if (this.application == null) {
                throw new IllegalArgumentException("Application context must not be null.");
            }

            if (isNullOrEmpty(apiKey)) {
                throw new IllegalArgumentException("API Key must not be null or empty.");
            }
            if (!validAPIKey(apiKey)) {
                throw new IllegalArgumentException("Invalid API Key.");
            }
            this.apiKey = apiKey;
        }

        /** Set a {@link LogLevel} for this instance. */
        public Builder logLevel(LogLevel logLevel) {
            if (logLevel == null) {
                throw new IllegalArgumentException("LogLevel must not be null.");
            }
            this.logLevel = logLevel;
            return this;
        }

        /** Automatically track screen view when activities are resumed. */
        public Builder autoTrackScreen() {
            this.autoTrackScreen = true;
            return this;
        }

        /**
         * Set the position for surveys as a whole.
         * @param position See {@link Position}
         * @return {@link Builder}
         */
        public Builder setPosition(Position position) {
            if (position == null){
                throw new IllegalArgumentException("Position must not be null.");
            }
            this.position = position;
            return this;
        }

        /**
         * Set color for the background. Working only for Phones.
          * @param color See {@link Color}
         * @return {@link Builder}
         */
        public Builder setbackgroundColor(int color) {
            this.backgroundColor = color;
            return this;
        }

        /**
         * Set custom CSS style for survey.
         * @param customStyle String URL to file
         * @return {@link Builder}
         */
        public Builder setCustomCSSStyle(String customStyle) {
            this.customStyleURL = customStyle;
            return this;
        }

        /** Create a {@link Qualaroo} client. */
        public Qualaroo build() {
            if (logLevel == null) {
                logLevel = LogLevel.NONE;
            }
            Logger logger = Logger.with(logLevel);

            return new Qualaroo(context, apiKey, logger, autoTrackScreen, position, backgroundColor, customStyleURL);
        }
    }
    //endregion

    //region ConnectivityReceiverListener

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

        if (isConnected && !isLoadedScript) {
            loadQualarooScript();
        }
        if (isConnected && unfulfilledReportRequests != null && unfulfilledReportRequests.size() > 0) {
            attemptToDeliveryUnfulfilledReportRequests();
        }
    }
    //endregion

    //region WebViewClient
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        if (url.equals(webView.getUrl())) {
            loadQualarooScript();
        }
        webView.clearCache(true);
    }
    //endregion

    //region LogLevel
    /** Controls the level of logging. */
    public enum LogLevel {
        /** No logging. */
        NONE,
        /** Log exceptions only. */
        INFO,
        /** Log exceptions and print debug output. */
        DEBUG,
        /** Same as {@link LogLevel#DEBUG}, and log transformation in bundled integrations. */
        VERBOSE;

        public boolean log() {
            return this != NONE;
        }
    }
    //endregion

    //region SurveyPosition
    /**
     * `QualarooSurveyPosition` enumerates all supported attachment position for a Qualaroo Mobile survey.
     *
     * Please notice supported position vary depending on the platform.
     */
    public enum Position {

        /**
         * Attach survey at the top of the host Activity's view.
         */
        TOP,

        /**
         * Attach survey at the bottom of the host Activity's view.
         */
        BOTTOM,

        /**
         * Attach survey at the top left corner of the host Activity's view.
         *
         * <p>Tablet only
         */
        TOP_LEFT,

        /**
         * Attach survey at the top right corner of the host Activity's view.
         *
         * <p>Tablet only
         */
        TOP_RIGHT,

        /**
         * Attach survey at the bottom left corner of the host Activity's view.
         *
         * <p>Tablet only
         */
        BOTTOM_LEFT,

        /**
         * Attach survey at the bottom right corner of the host Activity's view.
         *
         * <p>Tablet only
         */
        BOTTOM_RIGHT
    }
    //endregion
}
