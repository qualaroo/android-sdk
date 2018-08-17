package com.qualaroo.internal;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RestrictTo;
import android.util.DisplayMetrics;

import com.qualaroo.BuildConfig;

import java.util.Locale;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class SdkSession {

    private final String os = "Android";
    private final String sdkVersion = BuildConfig.VERSION_NAME;
    private final String language = Locale.getDefault().getLanguage();
    private final String deviceModel = Build.MODEL;
    private final String androidVersion = String.valueOf(Build.VERSION.SDK_INT);
    private final String deviceType;
    private final String resolution;
    private final String appName;

    public SdkSession(Context context, DeviceTypeMatcher.DeviceTypeProvider deviceTypeProvider) {
        this.appName = context.getPackageName();
        this.deviceType = deviceTypeProvider.deviceType();
        this.resolution = getResolution(context);
    }

    public String sdkVersion() {
        return sdkVersion;
    }

    public String appName() {
        return appName;
    }

    public String deviceModel() {
        return deviceModel;
    }

    public String androidVersion() {
        return androidVersion;
    }

    public String os() {
        return os;
    }

    public String language() {
        return language;
    }

    public String deviceType() {
        return deviceType;
    }

    public String resolution() {
        return resolution;
    }

    private String getResolution(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return String.format(Locale.ROOT, "%dx%d", metrics.widthPixels, metrics.heightPixels);
    }
}
