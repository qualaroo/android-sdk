package com.qualaroo.internal;

import android.content.Context;
import android.os.Build;

import com.qualaroo.BuildConfig;

public class SessionInfo {

    private static final String SDK_VERSION = BuildConfig.VERSION_NAME;

    private final String appName;
    private final String deviceType;
    private final String androidVersion;

    public SessionInfo(Context context) {
        this.appName = context.getPackageName();
        this.deviceType = Build.MODEL;
        this.androidVersion = String.valueOf(Build.VERSION.SDK_INT);
    }

    public String sdkVersion() {
        return SDK_VERSION;
    }

    public String appName() {
        return appName;
    }

    public String deviceType() {
        return deviceType;
    }

    public String androidVersion() {
        return androidVersion;
    }
}
