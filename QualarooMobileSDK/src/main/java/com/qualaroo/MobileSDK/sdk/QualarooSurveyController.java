package com.qualaroo.MobileSDK.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Base64;
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
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.qualaroo.MobileSDK.QMBackgroundColor;
import com.qualaroo.MobileSDK.QMPosition;
import com.qualaroo.MobileSDK.QMState;
import com.qualaroo.MobileSDK.QMReport;
import com.qualaroo.MobileSDK.sdk.API.QMRequest;
import com.qualaroo.MobileSDK.QMCallback;
import com.qualaroo.MobileSDK.sdk.Model.QMSurvey;
import com.qualaroo.MobileSDK.sdk.View.QMBlurView;
import com.qualaroo.MobileSDK.sdk.View.QMBlurViewInterface;
import com.qualaroo.MobileSDK.sdk.View.QMWebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static android.view.Gravity.BOTTOM;
import static android.view.Gravity.CENTER_HORIZONTAL;
import static android.view.Gravity.CENTER_VERTICAL;
import static android.view.Gravity.END;
import static android.view.Gravity.START;
import static android.view.Gravity.TOP;
import static com.qualaroo.MobileSDK.sdk.QMShowSurvey.QualarooShowSurveyDefault;
import static com.qualaroo.MobileSDK.sdk.QMShowSurvey.QualarooShowSurveyOnce;
import static com.qualaroo.MobileSDK.sdk.QMShowSurvey.QualarooShowSurveyPersistent;

/**
 * Created by Artem Orynko on 23.08.16.
 * Copyright © 2016 Qualaroo. All rights reserved.
 */

public class QualarooSurveyController extends WebViewClient implements QMBlurViewInterface {

    private static String baseURL = "file:///android_asset/qualaroo_host.html";

    private Context context;

    private String scriptURL;

    private boolean executing;
    private boolean cancelled;
    private boolean warning;
    private boolean ready;

    private QMWebView webView = null;
    private QMBlurView blurView = null;
    private QMRequest request = null;

    private QMPosition position;

    private float suggestedHeight;
    private String identity = null;

    private QMState state;
    private QMReport report;

    private Activity hostingActivity;

    private Thread loadQualarooScriptThread;
    private Thread setupRequestThread;

    private HashMap<String, QMSurvey> surveys = new HashMap<>();

    // region Accessors
    private boolean isTablet() {
        return (this.context.getResources().getConfiguration().screenLayout
            & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    void setExecuting(boolean newValue) {
        executing = newValue;
    }

    void setReady(boolean newValue) {
        ready = newValue;
    }

    public QMState getState() {
        return this.state;
    }

    public QMReport getReport() {
        return this.report;
    }

    QMWebView getWebView() {
        return webView;
    }

    QMBlurView getBlurView() { return blurView; }
    // endregion

    public QualarooSurveyController(Context context) {
        this.context = context;
    }

    public QualarooSurveyController init(String apiKey, String apiSecretKey) throws QMUtils {

        // Decode API Key
        if (!decodeAPIKey(apiKey)) throw new QMUtils(QMException.INVALID_API_KEY);

        // Start executing
        executing = true;
        cancelled = false;
        warning = false;
        ready = false;

        changeState();

        report = QMReport.ALL_IS_WELL;

        if (!apiSecretKey.equals("")) {
            setupRequest(apiSecretKey);
        }

        setSuggestedHeight(0);

        if (webView == null)
            setupViews();

        return this;
    }

    public boolean attachToActivity(Activity activity, QMPosition position, QMCallback QMCallback) {

        if (blurView.getParent() == activity) {
            warning = true;
            report = QMReport.ATTACHED;

            changeState();

            setCallback(QMCallback);
            return false;
        }

        if (!isTablet()
                && (position != QMPosition.TOP
                    && position != QMPosition.BOTTOM)) {
            warning = true;
            report = QMReport.POSITION_NOT_SUPPORTED;

            changeState();

            setCallback(QMCallback);
            return false;
        }

        blurView.setGravity(getGravity(position));

        if (webView.getParent() != blurView) {
            blurView.addView(webView);
        }

        activity.addContentView(blurView, blurView.getLayoutParams());

        hostingActivity = activity;
        this.position = position;

        return true;
    }

    public boolean removeFromActivity() {

        boolean wasAttachedToActivity;
        ViewParent viewParent;

        viewParent = blurView.getParent();
        wasAttachedToActivity = viewParent != null;

        if (wasAttachedToActivity) {
            ((ViewGroup) viewParent).removeView(blurView);
        }

        return wasAttachedToActivity;
    }

    public void close() {

        cancel();
        blurView.removeAllViews();
        blurView.delegate = null;
        blurView = null;
        webView.destroy();
    }

    public boolean setBlurViewBackgroundColor(QMBackgroundColor style, int alpha) {

        if (blurView == null || isTablet()) return false;

        int color;

        // init params for the chosen style
        switch (style) {
            case DARK:
                color = 0;
                break;
            case GREY:
                color = 128;
                break;
            case LIGHT:
                color = 255;
                break;
            default:
                color = 0;
                break;
        }

        // set background color for blurView
        blurView.setBackgroundColor(Color.argb(alpha, color, color, color));

        return true;
    }

    public void setIdentityCode(String identityCode) {
        identity = identityCode;
    }

    @Override
    public void handleTap() {
        evaluateJavaScript("_kiq.push(['stopSurvey']);", null);
    }

    private int getSuggestedHeight() {
        return (int) suggestedHeight;
    }

    void setSuggestedHeight(float suggestedHeight) {
        WindowManager windowManager = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int newSuggestedHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                suggestedHeight,
                this.context.getResources().getDisplayMetrics()
        );
        int halfScreenHeight = size.y / 2;

        if (halfScreenHeight < newSuggestedHeight) {
            this.suggestedHeight = halfScreenHeight;
        } else {
            this.suggestedHeight = newSuggestedHeight;
        }
    }

    void cancel() {
        executing = false;
        cancelled = true;
        warning = false;
        ready = false;

        if (loadQualarooScriptThread != null)
            loadQualarooScriptThread.interrupt();
        if (setupRequestThread != null)
            setupRequestThread.interrupt();

        changeState();
    }

    void changeState() {
        if (cancelled)
            state = QMState.CANCELLED;
        if (executing)
            state = QMState.EXECUTING;
        if (ready)
            state = QMState.READY;
        if (warning)
            state = QMState.WARNING;
    }

    public void showSurvey(final String surveyAlias, final boolean shouldForce, final QMCallback callback) {

        if (cancelled) {
            changeState();
            setCallback(callback);
            return;
        }

        if (hostingActivity == null) {
            warning = true;
            report = QMReport.NOT_ATTACHED;

            changeState();
            setCallback(callback);
            return;
        }

        if (!ready) {
            report = QMReport.NOT_READY;

            setCallback(callback);
            return;
        }

        if (!surveys.containsKey(surveyAlias)) {
            warning = true;
            report = QMReport.ALIAS_NOT_EXIST;

            changeState();

            setCallback(callback);
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (cancelled) return;

                try {
                    loadQualarooScriptThread.join();
                } catch (InterruptedException ignore) {}

                if (!isShowSurveyByAlias(surveyAlias)) {
                    setCallback(callback);
                    return;
                }

                String jsScript = "triggerSurvey('" + surveyAlias + "', " + shouldForce + ")";
                evaluateJavaScript(jsScript, null);
            }
        }).start();
    }

    private boolean isShowSurveyByAlias(String alias) {

        QMSurvey survey = surveys.get(alias);

        if (survey.howOftenShowSurvey == QMShowSurvey.QualarooShowSurveyOnce) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String identities = sharedPreferences.getString(alias, "");
            if (identities.contains(identity)) {
                report = QMReport.SHOWN;

                changeState();
                return false;
            }
        } else if (survey.howOftenShowSurvey == QMShowSurvey.QualarooShowSurveyDefault) {
            if (survey.identity.contains(identity)) {
                report = QMReport.ANSWERED;

                changeState();
                return false;
            }
        }
        return true;
    }

    private boolean decodeAPIKey(String APIKey) {

        byte[] dataFromAPIKey;

        try {
            dataFromAPIKey = Base64.decode(APIKey, Base64.DEFAULT);
        } catch (Exception ignore) {
            return false;
        }
        String stringFromData = new String(dataFromAPIKey);

        if (stringFromData == null) return false;

        JSONObject jsonObject;
        String relativeUrlPath;
        int version;
        try {
            jsonObject = new JSONObject(stringFromData);
            version = jsonObject.getInt("v");
            relativeUrlPath = jsonObject.getString("u");
        } catch (JSONException ignore) {
            return false;
        }

        if (version != 1) {
            return false;
        }

        this.scriptURL = "https://s3.amazonaws.com/" + relativeUrlPath;

        return true;
    }

    private void setupViews() {

        // Initialize WebView
        this.webView = new QMWebView(this.context);

        // Set params
        this.webView.setWebViewClient(this);
        this.webView.addJavascriptInterface(
                new QMJavaScriptInterface(this),
                "QualarooMobile"
        );

        // Load HTML
        this.webView.loadUrl(baseURL);

        // Initialize BlurView
        this.blurView = new QMBlurView(this.context).init(isTablet());

        this.blurView.delegate = this;

        if (isTablet())
            this.blurView.setBackgroundColor(Color.TRANSPARENT);
    }

    private void setupRequest(final String secretKey) {

        final String appKey = scriptURL.split("/")[4];

        setupRequestThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (cancelled) return;

                try {
                    request = new QMRequest(appKey, secretKey);
                } catch (Exception e) {
                    report = QMReport.INVALID_API_SECRET_KEY;
                    cancel();
                }
            }
        });
        setupRequestThread.start();
    }

    private int getGravity(QMPosition position) {
        int result = 0;

        switch (position) {
            case TOP:
                if (isTablet()) {
                    result = TOP|CENTER_HORIZONTAL;
                } else {
                    result = TOP;
                }
                break;
            case BOTTOM:
                if (isTablet()) {
                    result = BOTTOM|CENTER_HORIZONTAL;
                } else {
                    result = BOTTOM;
                }
                break;
            case LEFT:
                result = START|CENTER_VERTICAL;
                break;
            case RIGHT:
                result = END|CENTER_VERTICAL;
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

    private void setCallback(QMCallback QMCallback) {
        if (QMCallback == null) return;
        QMCallback.callback(state, report);
    }

    void setupIdentityCode() {
        if (identity == null) {
            identity = Settings.Secure.getString(this.context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        if (cancelled) return;

        String jsScript = "_kiq.push(['identify', '" + identity + "']);";

        evaluateJavaScript(jsScript, null);
    }

    void getSurveyInfo(String surveyInfo) {

        JSONObject aliases = null;
        JSONObject surveyRequireMaps = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(surveyInfo);
            aliases = jsonObject.getJSONObject("surveyAliases");
            surveyRequireMaps = jsonObject.getJSONObject("surveyRequireMaps");
        } catch (JSONException ignore) {}

        assert aliases != null;
        Iterator<String> keys = aliases.keys();
        final ArrayList<String> threadNames = new ArrayList<>();

        while (keys.hasNext()) {

            String alias = keys.next();
            String id = "";

            try {
                id = aliases.getString(alias);
            } catch (JSONException ignore) {}

            final QMSurvey survey = new QMSurvey(alias, id);

            JSONObject howOftenShowSurvey = null;

            try {
                assert surveyRequireMaps != null;
                howOftenShowSurvey = new JSONObject(
                        surveyRequireMaps.get(aliases.getString(alias)).toString()
                );
            } catch (JSONException ignore) {}

            assert howOftenShowSurvey != null;
            if (howOftenShowSurvey.toString().contains("_is_persistent_")) {
                survey.howOftenShowSurvey = QualarooShowSurveyPersistent;
            } else if (howOftenShowSurvey.toString().contains("_is_one_shot_")) {
                survey.howOftenShowSurvey = QualarooShowSurveyOnce;
            } else {
                survey.howOftenShowSurvey = QualarooShowSurveyDefault;
            }

            surveys.put(alias, survey);

            if (survey.howOftenShowSurvey == QualarooShowSurveyDefault && request != null) {

                final String threadName = "com.qualaroo." + alias;

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            setupRequestThread.join();

                        } catch (InterruptedException ignore) {}

                        try {
                            request.requestSurvey(survey);
                        } catch (Exception e) {
                            cancel();
                        }
                        threadNames.remove(threadName);

                    }
                });

                threadNames.add(threadName);
                thread.setName(threadName);

                thread.start();
            }
        }
        while (!threadNames.isEmpty()){}

        this.loadQualarooScriptThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (cancelled) return;
                ready = true;
                executing = false;
                report = QMReport.ALL_IS_WELL;

                changeState();
            }
        });
        loadQualarooScriptThread.start();

    }

    void reloadData() {
        evaluateJavaScript("location.reload(true);", null);
    }

    void updateHeight() {

        int newHeight = getSuggestedHeight();
        final LinearLayout.LayoutParams params;

        params = (LinearLayout.LayoutParams) webView.getLayoutParams();
        params.height = newHeight;

        if (isTablet())
            params.width = context.getResources().getDisplayMetrics().widthPixels / 2;

        hostingActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.setLayoutParams(params);
            }
        });
    }

    void performShowSurveyAnimation() {

        hostingActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                blurView.setVisibility(View.VISIBLE);
                backgroundAnimation(0.2f, 0.9f);

                TranslateAnimation animation;

                animation = getShowAnimation();
                animation.setDuration(500);
                animation.setFillAfter(true);

                webView.startAnimation(animation);
                webView.setVisibility(View.VISIBLE);
            }
        });
    }

    void performHideSurveyAnimation() {
        performHideSurveyAnimation(50);
    }

    private void performHideSurveyAnimation(final int duration) {

        hostingActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TranslateAnimation animation;

                animation = getHideAnimation();
                animation.setDuration(duration);
                animation.setFillAfter(true);

                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        hostingActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                backgroundAnimation(0.9f, 0.0f);
                            }
                        });
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        blurView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                webView.startAnimation(animation);
                webView.setVisibility(View.GONE);
            }
        });

    }

    private void backgroundAnimation(float start, float end) {
        AlphaAnimation animation = new AlphaAnimation(start, end);
        animation.setDuration(500);
        animation.setFillAfter(false);
        blurView.startAnimation(animation);
        blurView.setVisibility(View.VISIBLE);
    }

    private TranslateAnimation getShowAnimation() {

        int height = webView.getHeight();
        int width = webView.getWidth();

        switch (position) {
            case TOP:
                return new TranslateAnimation(0, 0, -height, 0);
            case BOTTOM:
                return new TranslateAnimation(0, 0, height, 0);
            case LEFT:
                return new TranslateAnimation(-width, 0, 0, 0);
            case RIGHT:
                return new TranslateAnimation(width, 0, 0, 0);
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

    private TranslateAnimation getHideAnimation() {

        int height = webView.getHeight();
        int width = webView.getWidth();

        switch (position) {
            case TOP:
                return new TranslateAnimation(0, 0, 0, -height);
            case BOTTOM:
                return new TranslateAnimation(0, 0, 0, height);
            case LEFT:
                return new TranslateAnimation(0, -width, 0, 0);
            case RIGHT:
                return new TranslateAnimation(0, width, 0, 0);
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

    @Override
    public void onPageFinished(final WebView view, final String url) {
        super.onPageFinished(view, url);

        if (state == QMState.READY) {

            ready = false;
            executing = true;
            warning = false;

            changeState();
        }

        // Load Qualaroo script ahead to time
        if (!cancelled) {

            loadQualarooScriptThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (url.equals(url))
                        loadQualarooScript();
                }
            });

            loadQualarooScriptThread.start();

            webView.clearCache(true);

        }
    }

    private void loadQualarooScript() {

        String jsScript = "loadQualarooScriptIfNeeded('" + scriptURL + "');";
        evaluateJavaScript(jsScript, null);
    }

    void evaluateJavaScript(final String script, final ValueCallback<String> callback) {

        this.hostingActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(script, callback);
                } else {
                    webView.loadUrl("javascript:" + script);
                }
            }
        });
    }

    void addInfoAboutSurveyAlias(String alias) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String identities = sharedPreferences.getString(alias, "");
        if (!identities.contains(identity)) {
            identities += identity + ";";
        }
        sharedPreferences.edit().putString(alias, identities).apply();
    }

    // Set interaction with screen
    void setInteraction(View view, final boolean bool) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return bool;
            }
        });
    }

}

