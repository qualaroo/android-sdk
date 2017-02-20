package com.qualaroo.MobileSDK.sdk.View;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.qualaroo.MobileSDK.BuildConfig;

/**
 * Created by Artem Orynko on 09.12.16.
 * Copyright © 2016 Qualaroo. All rights reserved.
 */

public class QMWebView extends WebView {

    public QMWebView(Context context) {

        super(context);

        String versionSDK = BuildConfig.VERSION_NAME;
        WebSettings webSettings = this.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(false);
        webSettings.setUserAgentString("AnddroidMobileSDK v" + versionSDK);

        setBackgroundColor(Color.TRANSPARENT);
        setVisibility(INVISIBLE);
        setWebChromeClient(new WebChromeClient());

        LinearLayout.LayoutParams params;

        params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0);

        setLayoutParams(params);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            setWebContentsDebuggingEnabled(true);
    }
}