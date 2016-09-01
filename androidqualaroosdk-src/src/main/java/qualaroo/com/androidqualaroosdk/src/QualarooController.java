package qualaroo.com.androidqualaroosdk.src;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.InflateException;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Artem Orynko on 23.08.16.
 * Copyright © 2016 Qualaroo. All rights reserved.
 */

public class QualarooController {

    // Tag for debug
    private static final String TAG = QualarooController.class.getSimpleName();

    private static final String mBaseURL = "file:///android_asset/qualaroo_host.html";

    private Activity mHostActivity;

    private String  mScriptURL;
    protected float   mSuggestedHeight;
    private boolean mKeyboardShowing;
    protected boolean mMinimized;
    private boolean mHtmlLoaded;

    LinearLayout    mLinearLayout;
    WebView         mWebView;

    //region Protected Methods

    protected QualarooController initWithAPIKey(String APIKey, Activity activity) {

        // Decode API key
        if (!decodeAPIKey(APIKey)) {
            //TODO: print error
            return null;
        }

        // Initialize properties
        mSuggestedHeight = 0;
        mKeyboardShowing = false;
        mMinimized = false;
        mHtmlLoaded = false;

        mHostActivity = activity;

        //TODO: OBserve to keyboard show/hide

        return this;
    }

    protected boolean attachToActivity(QualarooSurveyPosition surveyPosition) {

        boolean isTablet = mHostActivity.getResources().getBoolean(R.bool.isTablet);
        QualarooSurveyPosition position;

        if (isTablet) {
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

        setupLinearLayout(getPosition(surveyPosition));

        setupWebView();

        mLinearLayout.addView(mWebView);

        mWebView.loadUrl(mBaseURL);

        //TODO: constaint

        return true;
    }

    protected void showSurvey(String surveyAlias, boolean shouldForce) {

        if (mHostActivity == null) {
            Log.d(TAG, "EROOR: Survey not attached to any Activity");
            return;
        }

        mLinearLayout.setVisibility(View.VISIBLE);

        loadQualarooScriptIfNeeded();

        String jsString;

        jsString = "triggerSurvey('" + surveyAlias + "', " + shouldForce + ")";

        mWebView.evaluateJavascript(jsString, null);
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

    private void setupWebView() {

        mWebView = new WebView(mHostActivity);

        mWebView.setBackgroundColor(Color.TRANSPARENT);
        mWebView.setVisibility(View.INVISIBLE);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.addJavascriptInterface(new QualarooJavaScriptInterface(mHostActivity), "Qualaroo");

        WebSettings webSettings;
        webSettings = mWebView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(false);

        LinearLayout.LayoutParams layoutParams;
        layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                300//(int) mSuggestedHeight
        );

        mWebView.setLayoutParams(layoutParams);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (url.equals(mBaseURL)) {
                    mHtmlLoaded = true;

                    loadQualarooScriptIfNeeded();
                }
            }
        });
    }

    private int getPosition(QualarooSurveyPosition position) {

        switch (position) {
            case QUALAROO_SURVEY_POSITION_TOP:
                return Gravity.TOP;
            case QUALAROO_SURVEY_POSITION_BOTTOM:
                return Gravity.BOTTOM;
            case QUALAROO_SURVEY_POSITION_TOP_LEFT:
                return Gravity.TOP | Gravity.START;
            case QUALAROO_SURVEY_POSITION_TOP_RIGHT:
                return Gravity.TOP | Gravity.END;
            case QUALAROO_SURVEY_POSITION_BOTTOM_LEFT:
                return Gravity.BOTTOM | Gravity.START;
            case QUALAROO_SURVEY_POSITION_BOTTOM_RIGTH:
                return Gravity.BOTTOM | Gravity.END;
            default:
                break;
        }

        return 0;
    }

    private void setupLinearLayout(int position) {

        mLinearLayout = new LinearLayout(mHostActivity);

        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.setBackgroundColor(Color.TRANSPARENT);
//        mLinearLayout.setVisibility(View.INVISIBLE);
        mLinearLayout.setGravity(position);

        LinearLayout.LayoutParams layoutParams;

        layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        mHostActivity.addContentView(mLinearLayout, layoutParams);

        mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLinearLayout.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void loadQualarooScriptIfNeeded(){

        final Handler handler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mWebView.evaluateJavascript(mScriptURL, null);
                        }
                    });
                } catch (InflateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    //endregion
}
