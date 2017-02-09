package qualaroo.com.AndroidMobileSDK.View;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;

import qualaroo.com.AndroidMobileSDK.BuildConfig;

/**
 * Created by Artem Orynko on 09.12.16.
 * Copyright © 2016 Qualaroo. All rights reserved.
 */

public class QMWebView extends WebView {
    Context mContext;

    public QMWebView(Context context) {
        super(context);
        mContext = context;
    }

    public void init(int suggestedHeight) {
        WebSettings webSettings;
        webSettings = this.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(false);

        String versionSDK = BuildConfig.VERSION_NAME;
        webSettings.setUserAgentString("AndroidMobileSDK v" + versionSDK);

        this.setBackgroundColor(Color.TRANSPARENT);
        this.setVisibility(View.INVISIBLE);
        this.setWebChromeClient(new WebChromeClient());
        LinearLayout.LayoutParams layoutParams;

        layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                suggestedHeight
        );

        this.setLayoutParams(layoutParams);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setWebContentsDebuggingEnabled(true);
        }
    }
}


