package qualaroo.com.AndroidMobileSDK;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import static android.view.Gravity.*;

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

    private String  mScriptURL;
    protected float   mSuggestedHeight;
    protected boolean mMinimized;
    protected boolean mHtmlLoaded;
    protected boolean mQualarooScriptLoaded;
    private QualarooSurveyPosition mPosition;

    LinearLayout    mLinearLayout;
    WebView         mWebView;

    //region Protected Methods

    protected QualarooController(Activity hostActivity, QualarooSurvey delegate) {
        mDelegate = delegate;
        mHostActivity = hostActivity;
        mIsTablet = mHostActivity.getResources().getBoolean(R.bool.isTablet);
    }

    protected QualarooController initWithAPIKey(String APIKey) {

        // Decode API key
        if (!decodeAPIKey(APIKey)) {
            //TODO: print error
            return null;
        }

        // Initialize properties
        mSuggestedHeight = 0;
        mMinimized = false;
        mHtmlLoaded = false;

        //TODO: OBserve to keyboard show/hide

        initializeWebView();

        return this;
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

        //TODO: constaint

        return true;
    }

    protected boolean removeFromActivity() {

        boolean wasAttachedToActivity;

        ViewParent view = mLinearLayout.getParent();
        wasAttachedToActivity = view != null;

        mLinearLayout.removeAllViews();
        mWebView.clearCache(true);
        mWebView.destroy();
        mWebView = null;

        if (wasAttachedToActivity) {
            ((ViewGroup) view).removeView(mLinearLayout);
            mLinearLayout = null;
        }

        return true;
    }

    protected void showSurvey(String surveyAlias, boolean shouldForce) {

        if (mHostActivity == null) {
            Log.d(TAG, "EROOR: Survey not attached to any Activity");
            return;
        }

        loadQualarooScriptIfNeeded();

        if (mQualarooScriptLoaded) {
            String jsString;

            jsString = "triggerSurvey('" + surveyAlias + "', " + shouldForce + ")";

            mWebView.evaluateJavascript(jsString, null);
        }
    }

    //endregion

    //region Private Methods

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

        mLinearLayout = new LinearLayout(mHostActivity);

        mLinearLayout.setId(R.id.my_linear_layout);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.setBackgroundColor(Color.TRANSPARENT);
        mLinearLayout.setVisibility(View.INVISIBLE);
        mLinearLayout.setGravity(getPosition(mPosition));

        if (mWebView == null) {
            initializeWebView();
        }

        mLinearLayout.addView(mWebView);

        LinearLayout.LayoutParams layoutParams;
        layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        mLinearLayout.setLayoutParams(layoutParams);

        mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performHideSurveyAnimation(500);
            }
        });
    }

    private void initializeWebView() {
        if (mWebView != null) {
            return;
        }

        mWebView = new WebView(mHostActivity);
        mWebView.setId(R.id.my_webview);
        mWebView.setBackgroundColor(Color.TRANSPARENT);
        mWebView.setVisibility(View.INVISIBLE);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (url.equals(mBaseURL)) {
                    loadQualarooScriptIfNeeded();
                    mHtmlLoaded = true;
                }
            }
        });
        mWebView.addJavascriptInterface(
                new QualarooJavaScriptInterface(mHostActivity, this),
                "Qualaroo"
        );

        WebSettings webSettings;
        webSettings = mWebView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(false);

        LinearLayout.LayoutParams layoutParams;

        layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        mSuggestedHeight,
                        mHostActivity.getResources().getDisplayMetrics()
                )
        );

        mWebView.setLayoutParams(layoutParams);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.setWebContentsDebuggingEnabled(true);
        }
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

    private void loadQualarooScriptIfNeeded(){

        String jsString = "loadQualarooScriptIfNeeded('" + mScriptURL + "');";
        mWebView.evaluateJavascript(jsString, null);

    }
    //endregion

    //region Protected Methods

    protected void setupIdentityCode() {
        final String deviceUUID = UUID.randomUUID().toString();
        String jsString = "_kiq.push(['identify', '" + deviceUUID + "'])";

        mWebView.evaluateJavascript(jsString, null);
    }

    protected void updateHeight(){
        int newHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                mSuggestedHeight,
                mHostActivity.getResources().getDisplayMetrics()
        );
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

    protected void performShowSurveyAnimation() {
        mHostActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLinearLayout.setVisibility(View.VISIBLE);
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

    //endregion
}

