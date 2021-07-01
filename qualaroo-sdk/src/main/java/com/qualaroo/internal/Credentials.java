package com.qualaroo.internal;

import androidx.annotation.RestrictTo;

import okio.ByteString;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class Credentials {

    private final String apiKey;
    private final String apiSecret;
    private final String siteId;

    public Credentials(String apiKey) {
        if (apiKey == null || apiKey.length() == 0) {
            throw new InvalidCredentialsException();
        }
        ByteString byteKey = ByteString.decodeBase64(apiKey);
        if (byteKey == null) {
            throw new InvalidCredentialsException();
        }
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
