package com.qualaroo.internal;

import com.qualaroo.internal.storage.Settings;

import java.util.UUID;

public class UserInfo {

    private static final String KEY_USER_ID = "q.uid";
    private static final String KEY_DEVICE_ID = "q.did";

    private final Settings settings;

    public UserInfo(Settings settings) {
        this.settings = settings;
    }

    void setUserId(String userId) {
        settings.store(KEY_USER_ID, userId);
    }

    String getUserId() {
        return settings.get(KEY_USER_ID);
    }

    public String getDeviceId() {
        String deviceId = settings.get(KEY_DEVICE_ID);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            settings.store(KEY_DEVICE_ID, deviceId);
        }
        return deviceId;
    }

}
