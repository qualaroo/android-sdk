package com.qualaroo.MobileSDK;

/**
 * Created by Artem Orynko on 25.08.16.
 * Copyright © 2016 Qualaroo. All rights reserved.
 */

/**
 * `QMPosition` enumerates all supported attachment position for a Qualaroo Mobile survey.
 *
 * Please notice supported position vary depending on the platform.
 */
public enum QMPosition {

    /**
     * Attach survey at the top of the host Activity's view
     *
     */
    TOP,

    /**
     * Attach survey at the bottom of the host Activity's view
     *
     */
    BOTTOM,

    /**
     * Attach survey at the left of the host Activity's view
     *
     * Tablet only
     */
    LEFT,

    /**
     * Attach survey at the right of the host Activity's view
     *
     * Tablet only
     */
    RIGHT,

    /**
     * Attach survey at the top left corner of the host Activity's view
     *
     * Tablet only
     */
    TOP_LEFT,

    /**
     * Attach survey at the top right corner of the host Activity's view
     *
     * Tablet only
     */
    TOP_RIGHT,

    /**
     * Attach survey at the bottom right corner of the host Activity's view
     *
     * Tablet only
     */
    BOTTOM_LEFT,

    /**
     * Attach survey at the bottom right corner of the host Activity's view
     *
     * Tablet only     */
    BOTTOM_RIGHT;

    public static String toString(QMPosition position) {
        String result = "";

        switch (position) {
            case TOP:
                result = "Top";
                break;
            case BOTTOM:
                result = " Bottom";
                break;
            case LEFT:
                result = "Left center";
                break;
            case  RIGHT:
                result = "Right center";
                break;
            case TOP_LEFT:
                result = "Top left";
                break;
            case TOP_RIGHT:
                result = "Top right";
                break;
            case BOTTOM_LEFT:
                result = "Bottom left";
                break;
            case BOTTOM_RIGHT:
                result = "Bottom right";
                break;
        }

        return result;
    }
}