/*
 * Copyright © 2017 Qualaroo. All rights reserved.
 */

package com.qualaroo.views;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.qualaroo.BuildConfig;

/*
 * Created by Artem Orynko on 4/13/17.
 */

public final class QualarooWebView extends WebView {

    public QualarooWebView(Context context) {
        super(context);

        String versionSDK = BuildConfig.VERSION_NAME;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0
        );
        WebSettings webSettings = this.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(false);
        webSettings.setUserAgentString("AndroidSDK v" + versionSDK);

        setBackgroundColor(Color.TRANSPARENT);
        setVisibility(INVISIBLE);
        setWebChromeClient(new WebChromeClient());
        setLayoutParams(layoutParams);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            setWebContentsDebuggingEnabled(true);
    }

    public void evaluateJavaScript(final String script, final ValueCallback<String> callback) {
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    evaluateJavascript(script, callback);
                } else {
                    loadUrl("javascript:" + script);
                }
            }
        });
    }
}
