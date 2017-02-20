package com.qualaroo.MobileSDK;

/**
 * Created by Artem on 13.02.17.
 * Copyright © 2016 Qualaroo. All rights reserved.
 */

public enum QMReport {

    /**
     * All is well
     */
    ALL_IS_WELL,

    /**
     * Invalid API Secret key
     */
    INVALID_API_SECRET_KEY,

    /**
     * QualarooMobile is already attached to host Activity
     */
    ATTACHED,

    /**
     * QualarooMobile is not attached to any Activity
     */
    NOT_ATTACHED,

    /**
     * Supported position on this platform are TOP or BOTTOM
     */
    POSITION_NOT_SUPPORTED,

    /**
     * Alias doesn't exist
     */
    ALIAS_NOT_EXIST,

    /**
     * QualarooMobile is not ready yet
     */
    NOT_READY,

    /**
     * Customer is already answered for this survey
     */
    ANSWERED,
    /**
     * Survey already is shown for this customer
     */
    SHOWN;

    public static String toString(QMReport report) {
        String result = "";

        switch (report) {
            case INVALID_API_SECRET_KEY:
                result = "Invalid API Secret key";
                break;
            case ATTACHED:
                result = "QualarooMobile is already attached to host Activity";
                break;
            case NOT_ATTACHED:
                result = "QualarooMobile is not attached to any Activity";
                break;
            case POSITION_NOT_SUPPORTED:
                result = "Supported position on this platform are TOP or BOTTOM";
                break;
            case NOT_READY:
                result = "QualarooSurvey is not ready yet";
                break;
            case ALIAS_NOT_EXIST:
                result = "Alias doesn't exist";
                break;
            case ANSWERED:
                result = "Customer is already answered for this survey";
                break;
            case SHOWN:
                result = "Survey is already shown for this customer";
                break;
            default:
                result = "All right";
                break;
        }

        return result;
    }
}
