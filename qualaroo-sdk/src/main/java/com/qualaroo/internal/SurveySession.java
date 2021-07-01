package com.qualaroo.internal;

import java.util.UUID;

public class SurveySession {

    private final String uuid = UUID.randomUUID().toString();

    public String uuid() {
        return uuid;
    }
}
