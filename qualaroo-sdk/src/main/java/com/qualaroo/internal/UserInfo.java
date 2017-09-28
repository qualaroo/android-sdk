package com.qualaroo.internal;

import com.qualaroo.internal.storage.LocalStorage;
import com.qualaroo.internal.storage.Settings;

import java.util.Map;
import java.util.UUID;

public class UserInfo {

    private static final String KEY_USER_ID = "q.uid";
    private static final String KEY_DEVICE_ID = "q.did";

    private final Settings settings;
    private final LocalStorage localStorage;

    public UserInfo(Settings settings, LocalStorage localStorage) {
        this.settings = settings;
        this.localStorage = localStorage;
    }

    synchronized void setUserId(String userId) {
        settings.store(KEY_USER_ID, userId);
    }

    synchronized String getUserId() {
        return settings.get(KEY_USER_ID);
    }

    public synchronized String getDeviceId() {
        String deviceId = settings.get(KEY_DEVICE_ID);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            settings.store(KEY_DEVICE_ID, deviceId);
        }
        return deviceId;
    }

    public void setUserProperty(String key, String value) {
        localStorage.updateUserProperty(key, value);
    }

    public Map<String, String> getUserProperties() {
        return localStorage.getUserProperties();
    }

}
