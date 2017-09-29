package com.qualaroo.internal;

import android.support.annotation.RestrictTo;

import okio.ByteString;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class Credentials {

    private final String apiKey;
    private final String apiSecret;
    private final String siteId;

    public Credentials(String apiKey) {
        ByteString byteKey = ByteString.decodeBase64(apiKey);
        String key = byteKey.utf8();
        String[] keyParts = key.split(":");
        if (keyParts.length != 3) {
            throw new InvalidCredentialsException();
        }
        if (!isNumericOnly(keyParts[0])) {
            throw new InvalidCredentialsException();
        }
        if (!isNumericOnly(keyParts[2])) {
            throw new InvalidCredentialsException();
        }
        this.apiKey = keyParts[0];
        this.apiSecret = keyParts[1];
        this.siteId = keyParts[2];
    }

    public String apiKey() {
        return apiKey;
    }

    public String apiSecret() {
        return apiSecret;
    }

    public String siteId() {
        return siteId;
    }

    private static boolean isNumericOnly(String text) {
        char[] chars = text.toCharArray();
        for (char c : chars) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}
