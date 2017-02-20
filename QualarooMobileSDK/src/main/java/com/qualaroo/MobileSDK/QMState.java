package com.qualaroo.MobileSDK;

/**
 * Created by Artem on 13.02.17.
 * Copyright © 2016 Qualaroo. All rights reserved.
 */

public enum QMState {

    /**
     * QualarooMobile is currently executing
     */
    EXECUTING,

    /**
     * QualarooMobile cancelled
     */
    CANCELLED,

    /**
     * QualarooMobile has the warning
     */
    WARNING,

    /**
     * QualarooMobile is ready to display
     */
    READY;

    public static String toString(QMState state) {
        String result = "";
        switch (state) {
            case EXECUTING:
                result = "QualarooMobile is currently executing";
                break;
            case CANCELLED:
                result = "QualarooMobile cancelled";
                break;
            case WARNING:
                result = "QualarooMobile has the warning";
                break;
            case READY:
                result = "QualarooMobile is ready to display";
                break;
        }
        return result;
    }
}
