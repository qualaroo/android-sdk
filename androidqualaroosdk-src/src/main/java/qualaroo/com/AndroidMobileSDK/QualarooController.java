package qualaroo.com.AndroidMobileSDK;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import qualaroo.com.AndroidMobileSDK.Api.QMRequest;
import qualaroo.com.AndroidMobileSDK.Model.QMSurvey;
import qualaroo.com.AndroidMobileSDK.View.QMLinearLayout;
import qualaroo.com.AndroidMobileSDK.View.QMWebView;

import static android.view.Gravity.BOTTOM;
import static android.view.Gravity.LEFT;
import static android.view.Gravity.RIGHT;
import static android.view.Gravity.TOP;
//
/**
 * Created by Artem Orynko on 23.08.16.
 * Copyright © 2016 Qualaroo. All rights reserved.
 */

class QualarooController {

    // Tag for debug
    private static final String TAG = QualarooController.class.getSimpleName();

    private static String mBaseURL = "file:///android_asset/qualaroo_host.html";

    protected QualarooSurvey mDelegate;
    private Activity mHostActivity;
    private boolean mIsTablet;

    private String mScriptURL;
    private String mIdentity;
    String mSecretKey;
    private float mSuggestedHeight;
    protected boolean mMinimized;
    protected boolean mHtmlLoaded;
    protected boolean mQualarooScriptLoaded;
    protected boolean mSurveysLoaded;
    private QualarooSurveyPosition mPosition;
    protected HashMap<String, QMSurvey> mSurveys;
    protected QMRequest mRequest;

    QMLinearLayout mLinearLayout;
    QMWebView mWebView;

    //region Protected Methods

    protected int getSugesstedHeight() {
        return (int) mSuggestedHeight;
    }

    protected void setSuggestedHeight(float suggestedHeight) {
        WindowManager windowManager = (WindowManager) mHostActivity.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int newSuggestedHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                suggestedHeight,
                mHostActivity.getResources().getDisplayMetrics()
        );
        int halfScreenHeight = size.y / 2;

        if (halfScreenHeight < newSuggestedHeight) {
            mSuggestedHeight = halfScreenHeight;
        } else {
            mSuggestedHeight = newSuggestedHeight;
        }
    }

    protected String getIdentity() {
        if (mIdentity == null) {
            mIdentity = Settings.Secure.getString(mHostActivity.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        }
        return mIdentity;
    }

    protected void setIdentity(String mIdentity) {
        this.mIdentity = mIdentity;
    }

    protected QualarooController(Activity hostActivity, QualarooSurvey delegate) {
        mDelegate = delegate;
        mHostActivity = hostActivity;
        mIsTablet = mHostActivity.getResources().getBoolean(R.bool.isTablet);
    }

    protected QualarooController initWithAPIKey(String APIKey, String APISecretKey) {

        // Decode API key
        if (!decodeAPIKey(APIKey)) {
            Log.d(TAG, "Unable to obtain data from APIKey");
            return null;
        }

        // Initialize properties
        mSecretKey = APISecretKey;
        setSuggestedHeight(0);
        mMinimized = false;
        mHtmlLoaded = false;

        initializeWebView();

        return this;
    }

    protected boolean setBackgroundStyle(QualarooBackgroundStyle style, int opacity) {
        int color;

        //init params for for the chosen style
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
        }

        //set background color for QualarooController's layout
        mLinearLayout.setBackgroundColor(Color.argb(opacity, color, color, color));
        return true;
    }

    protected boolean attachToActivity(QualarooSurveyPosition surveyPosition) {

        if (mIsTablet) {
            if (surveyPosition == QualarooSurveyPosition.QUALAROO_SURVEY_POSITION_TOP
                    || surveyPosition == QualarooSurveyPosition.QUALAROO_SURVEY_POSITION_BOTTOM) {
                Log.d(TAG, "ERROR: Supported positions on this platform are Bottom Left, Bottom Right, Top Left and Top Right.");
                return false;
            }
        } else if (surveyPosition != QualarooSurveyPosition.QUALAROO_SURVEY_POSITION_TOP
                && surveyPosition != QualarooSurveyPosition.QUALAROO_SURVEY_POSITION_BOTTOM) {
            Log.d(TAG, "ERROR: Supported positions on this platform are Top or Bottom.");
            return false;
        }

        mPosition = surveyPosition;

        if (mLinearLayout == null) {
            initializeLinearLayout();
        }

        mWebView.setVisibility(View.INVISIBLE);
        mWebView.loadUrl(mBaseURL);

        mHostActivity.addContentView(mLinearLayout, mLinearLayout.getLayoutParams());

        if (mSecretKey != null) {
            QMRequest request = QMRequest.getInstance();
            request.mSecretKey = mSecretKey;
            request.mAppKey = mScriptURL.split("/")[4];

            mRequest = request;
        }

        return true;
    }

    protected boolean removeFromActivity() {

        boolean wasAttachedToActivity;

        ViewParent view = mLinearLayout.getParent();
        wasAttachedToActivity = view != null;

        mLinearLayout.removeAllViews();
        mWebView = null;

        if (wasAttachedToActivity) {
            ((ViewGroup) view).removeView(mLinearLayout);
            mLinearLayout = null;
        }

        return true;
    }

    protected void showSurvey(final String surveyAlias, final boolean shouldForce) {

        if (mHostActivity == null) {
            Log.d(TAG, "EROOR: Survey not attached to any Activity");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                int timeout = 100;

                while (!mHtmlLoaded) {
                    try {
                        Thread.sleep(timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                loadQualarooScriptIfNeeded();
                while (!mQualarooScriptLoaded) {
                    try {
                        Thread.sleep(timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (mSecretKey != null) {
                    while (!mSurveysLoaded) {
                        try {
                            Thread.sleep(timeout);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    boolean showSurvey = isShowSurveyByAlias(surveyAlias);
                    if (!showSurvey) {
                        return;
                    }
                }

                String jsString;

                jsString = "triggerSurvey('" + surveyAlias + "', " + shouldForce + ")";

                evaluateJavaScript(jsString, null);
            }
        }).start();

    }

    //endregion

    //region Private Methods

    private boolean isShowSurveyByAlias(String alias) {

        QMSurvey survey = mSurveys.get(alias);

        Log.d(TAG, "How often show survey: " + survey.howOftenShowSurvey);

        if (survey.howOftenShowSurvey == QMShowSurvey.QualarooShowSurveyOnce) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mHostActivity);
            String identities = sharedPreferences.getString(alias, "");
            if (identities.contains(mIdentity)) {
                Log.d(TAG, "A survey " + survey.alias + " has already shown to a client " + mIdentity);
                return false;
            }
        } else if (survey.howOftenShowSurvey == QMShowSurvey.QualarooShowSurveyDefault) {
            boolean isAnswered = survey.identity.contains(mIdentity);
            if (isAnswered) {
                Log.d(TAG, "A client " + mIdentity + " already answered to survey: " + survey.alias);
            }
            return !isAnswered;
        }
        return true;
    }

    private boolean decodeAPIKey(String APIKey) {

        byte[] dataFromAPIKey = Base64.decode(APIKey, Base64.DEFAULT);
        String sFromData = new String(dataFromAPIKey);

        if (sFromData == null) {
            Log.d(TAG, "Unable to obtain data from APIKey");
            return false;
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(sFromData);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "Error decoding JSON: ", e);
        }

        int version = 0;
        try {
            version = jsonObject.getInt("v");
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        String relativeURLPath = null;
        try {
            relativeURLPath = jsonObject.getString("u");
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        if (version != 1) {
            return false;
        }

        mScriptURL = "https://s3.amazonaws.com/" + relativeURLPath;

        return true;
    }

    private void initializeLinearLayout() {
        if (mLinearLayout != null) {
            return;
        }

        if (mWebView == null) {
            mWebView = new QMWebView(mHostActivity);
            mWebView.init(getSugesstedHeight());
        }

        mLinearLayout = new QMLinearLayout(mHostActivity);
        mLinearLayout.init(getPosition(mPosition), getOnClickListener());
        mLinearLayout.addView(mWebView);
    }

    private void initializeWebView() {
        if (mWebView != null) {
            return;
        }
        mWebView = new QMWebView(mHostActivity);
        mWebView.init(getSugesstedHeight());
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (url.equals(url)) {
                    loadQualarooScriptIfNeeded();
                    mHtmlLoaded = true;
                }
                view.clearCache(true);

            }
        });
        mWebView.addJavascriptInterface(
                new QualarooJavaScriptInterface(mHostActivity, this),
                "Qualaroo"
        );
    }

    private int getPosition(QualarooSurveyPosition position) {

        Integer result;
        switch (position) {
            case QUALAROO_SURVEY_POSITION_TOP:
                result = TOP;
                break;
            case QUALAROO_SURVEY_POSITION_BOTTOM:
                result = BOTTOM;
                break;
            case QUALAROO_SURVEY_POSITION_TOP_LEFT:
                result = TOP|LEFT;
                break;
            case QUALAROO_SURVEY_POSITION_TOP_RIGHT:
                result = TOP|RIGHT;
                break;
            case QUALAROO_SURVEY_POSITION_BOTTOM_LEFT:
                result = BOTTOM|LEFT;
                break;
            case QUALAROO_SURVEY_POSITION_BOTTOM_RIGTH:
                result = BOTTOM|RIGHT;
                break;
            default:
                result = 0;
                break;
        }

        return result;
    }

    private void loadQualarooScriptIfNeeded() {

        String jsString = "loadQualarooScriptIfNeeded('" + mScriptURL + "');";
        evaluateJavaScript(jsString, null);

    }
    //endregion

    //region Protected Methods

    protected void addInfoAboutSurveyAlias(String alias) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mHostActivity);
        String identities = sharedPreferences.getString(alias, "");
        if (!identities.contains(mIdentity)) {
            identities += mIdentity + ";";
        }
        sharedPreferences.edit().putString(alias, identities).apply();
    }

    protected void evaluateJavaScript(final String script, final ValueCallback<String> resultCallback) {

        mHostActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mWebView.evaluateJavascript(script, resultCallback);
                } else {
                    mWebView.loadUrl("javascript:" + script);
                }
            }
        });
    }

    protected void setupIdentityCode() {

        String jsString = "_kiq.push(['identify', '" + getIdentity() + "'])";

        evaluateJavaScript(jsString, null);
        Log.d(TAG, "Qualaroo Identity Code set to " + getIdentity());
    }

    protected void updateHeight() {
        int newHeight = getSugesstedHeight();
        final LinearLayout.LayoutParams layoutParams;
        layoutParams = (LinearLayout.LayoutParams) mWebView.getLayoutParams();
        layoutParams.height = newHeight;

        if (mIsTablet) {
            layoutParams.width = mHostActivity.getResources().getDisplayMetrics().widthPixels / 2;
        }
        mHostActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mWebView.setLayoutParams(layoutParams);
            }
        });
    }

    private View.OnClickListener getOnClickListener() {

        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performHideSurveyAnimation(500);
            }
        };
    }

    protected void performHideSurveyAnimation() {
        performHideSurveyAnimation(50);
    }

    protected void performHideSurveyAnimation(final int duration) {
        mHostActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TranslateAnimation animation;
                animation = getHideAnimation();
                animation.setDuration(duration);
                animation.setFillAfter(true);

                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        mHostActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                backgroundAnimation(0.9f, 0.0f);
                            }
                        });

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mLinearLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                mWebView.startAnimation(animation);
                mWebView.setVisibility(View.GONE);

            }
        });
    }

    private void backgroundAnimation(float start, float end) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(start, end);
        alphaAnimation.setDuration(500);
        alphaAnimation.setFillAfter(false);
        mLinearLayout.startAnimation(alphaAnimation);
        mLinearLayout.setVisibility(View.VISIBLE);

    }

    protected void performShowSurveyAnimation() {
        mHostActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLinearLayout.setVisibility(View.VISIBLE);
                backgroundAnimation(0.2f, 0.9f);
                TranslateAnimation animation;
                animation = getShowAnimation();
                animation.setDuration(500);
                animation.setFillAfter(true);

                mWebView.startAnimation(animation);
                mWebView.setVisibility(View.VISIBLE);
            }
        });
    }

    private TranslateAnimation getShowAnimation() {
        switch (mPosition) {
            case QUALAROO_SURVEY_POSITION_TOP:
                return new TranslateAnimation(0, 0, -mWebView.getHeight(), 0);
            case QUALAROO_SURVEY_POSITION_BOTTOM_RIGTH:
                return new TranslateAnimation(mWebView.getWidth(), 0, 0, 0);
            case QUALAROO_SURVEY_POSITION_TOP_RIGHT:
                return new TranslateAnimation(mWebView.getWidth(), 0, 0, 0);
            case QUALAROO_SURVEY_POSITION_BOTTOM_LEFT:
                return new TranslateAnimation(-mWebView.getWidth(), 0, 0, 0);
            case QUALAROO_SURVEY_POSITION_TOP_LEFT:
                return new TranslateAnimation(-mWebView.getWidth(), 0, 0, 0);
            default:
                return new TranslateAnimation(0, 0, mWebView.getHeight(), 0);
        }
    }

    private TranslateAnimation getHideAnimation() {
        switch (mPosition) {
            case QUALAROO_SURVEY_POSITION_TOP:
                return new TranslateAnimation(0, 0, 0, -mWebView.getHeight());
            case QUALAROO_SURVEY_POSITION_BOTTOM_RIGTH:
                return new TranslateAnimation(0, mWebView.getWidth(), 0, 0);
            case QUALAROO_SURVEY_POSITION_TOP_RIGHT:
                return new TranslateAnimation(0, mWebView.getWidth(), 0, 0);
            case QUALAROO_SURVEY_POSITION_BOTTOM_LEFT:
                return new TranslateAnimation(0, -mWebView.getWidth(), 0, 0);
            case QUALAROO_SURVEY_POSITION_TOP_LEFT:
                return new TranslateAnimation(0, -mWebView.getWidth(), 0, 0);
            default:
                return new TranslateAnimation(0, 0, 0, mWebView.getHeight());
        }
    }

    //set interaction with screen
    protected void setInteraction(View view, final boolean bool) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return bool;
            }
        });
    }

    //endregion
}
