package com.qualaroo.MobileSDK.sdk;

/**
 * Created by Artem on 10.02.17.
 * Copyright © 2016 Qualaroo. All rights reserved.
 */

public class QMUtils extends Exception {

    public QMUtils(QMException exception) {
        super(descriptionForException(exception), new Throwable("com.qualaroo.MobileSDK"));
    }

    private static String descriptionForException(QMException exception) {
        String description;

        switch (exception) {
            case INVALID_API_KEY:
                description = "Invalid API key";
                break;
            default:
                description = "Unknown exception";
                break;
        }

        return description;
    }
}
