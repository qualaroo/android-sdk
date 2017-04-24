/*
 * Copyright © 2017 Qualaroo. All rights reserved.
 */

package com.qualaroo.helpers;
/*
 * Created by Artem Orynko on 12.04.17.
 */

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Base64DataException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.content.Context.TELEPHONY_SERVICE;
import static android.content.pm.PackageManager.FEATURE_TELEPHONY;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.provider.Settings.Secure.ANDROID_ID;
import static android.provider.Settings.Secure.getString;

public final class Utils {

    /** returns true if the application has the given permission. */
    public static boolean hasPermission(Context context, String permission) {
        return context.checkCallingOrSelfPermission(permission) == PERMISSION_GRANTED;
    }

    /** Returns true if the string is null, or empty (once trimmed). */
    public static boolean isNullOrEmpty(CharSequence text) {
        return isEmpty(text) || getTrimmedLength(text) == 0;
    }

    /** Returns true if the API Key is valid. */
    public static boolean validAPIKey(String apiKey) {
        return decodeAPIKey(apiKey);
    }

    /** Get the string resource for the given key. Returns null if not found. */
    public static String getResourceString(Context context, String key) {
        int id = getIdentifier(context, "string", key);
        if (id != 0) {
            return context.getResources().getString(id);
        } else {
            return null;
        }
    }

    /** Get the script url fro the given key. */
    public static String getScriptURL(String key) {
        return decodeScriptURL(key);
    }

    /** Returns true if it is Tablet. */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                    >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /** Creates a unique device id. */
    public static String getDeviceId(Context context) {
        String androidId = getString(context.getContentResolver(), ANDROID_ID);
        if (!isNullOrEmpty(androidId)
                && !"9774d56d682e549c".equals(androidId)
                && !"unknown".equals(androidId)
                && !"000000000000000".equals(androidId)) {
            return androidId;
        }

        // Serial number, guaranteed to be on all non phones in 2.3+
        if (!isNullOrEmpty(Build.SERIAL)) {
            return Build.SERIAL;
        }

        // Telephony ID, guaranteed to be on all phones, requires READ_PHONE_STATE permission
        if (hasPermission(context, READ_PHONE_STATE) && hasFeatures(context, FEATURE_TELEPHONY)) {
            TelephonyManager telephonyManager = getSystemService(context, TELEPHONY_SERVICE);
            String telephonyId = telephonyManager.getDeviceId();
            if (!isNullOrEmpty(telephonyId)) {
                return telephonyId;
            }
        }

        // If this still fails, generate random identifier that does not persist across installations
        return UUID.randomUUID().toString();
    }

    /** Returns true if the application has the given features. */
    private static boolean hasFeatures(Context context, String feature) {
        return context.getPackageManager().hasSystemFeature(feature);
    }

    /** Returns the system service for the given string. */
    private static <T> T getSystemService(Context context, String serviceConstant) {
        return (T) context.getSystemService(serviceConstant);
    }

    /** Decode scriptURL from API Key. */
    private static String decodeScriptURL(String key) {
        String dataString = decodeStringFromData(key);
        String relativeURL = null;

        try {
            JSONObject jsonObject = new JSONObject(dataString);
            relativeURL = jsonObject.getString("u");
        } catch (JSONException ignored) {}

        return "https://s3.amazonaws.com/" + relativeURL;
    }

    /** Get the identifier for the resource with a given type and key. */
    private static int getIdentifier(Context context, String type, String key) {
        return context.getResources().getIdentifier(key, type, context.getPackageName());
    }

    /** Decode and check the API Key. */
    private static boolean decodeAPIKey(String apiKey) {
        String dataString = decodeStringFromData(apiKey);

        if (dataString == null) return false;
        try {
            JSONObject jsonObject = new JSONObject(dataString);
            if (jsonObject.getInt("v") != 1) return false;
            if (isNullOrEmpty(jsonObject.getString("u"))) return false;
        } catch (JSONException ignored) {
            return false;
        }
        return true;
    }

    /** Return decode the string from data */
    private static String decodeStringFromData(String string) {
        byte[] data;
        try {
            data = Base64.decode(string, Base64.DEFAULT);
        } catch (Exception ignored) {
            return null;
        }
        return new String(data);
    }
    /**
     * Returns true if the string is null or 0-length.
     *
     * <p>Copied from {@link TextUtils#isEmpty(CharSequence)}
     *
     * @param string the string to be examined
     * @return true if string is null or zero length
     */
    private static boolean isEmpty(@Nullable CharSequence string) {
        return string == null || string.length() == 0;
    }

    /**
     * Returns the length that the specified CharSequence would have if spaces and control characters
     * were trimmed from the start and end, as by {@link String#trim}.
     *
     * <p>Copied from {@link TextUtils#getTrimmedLength(CharSequence)}
     */
    private static int getTrimmedLength(@NonNull CharSequence string) {
        int length = string.length();

        int start = 0;
        while (start < length && string.charAt(start) <= ' ') {
            start++;
        }

        int end = length;
        while (end > start && string.charAt(end - 1) <= ' ') {
            end--;
        }

        return end - start;
    }
}
