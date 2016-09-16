package qualaroo.com.AndroidMobileSDK;

/**
 * Created by Artem Orynko on 25.08.16.
 * Copyright © 2016 Qualaroo. All rights reserved.
 */

/**
 * `QualarooSurveyPosition` enumerates all supportes attachment position for a Qualaroo Mobile survey.
 *
 * Please notice supported position vary depending on the platform.
 */
public enum QualarooSurveyPosition {

    /**
     * Attach survey at the top of the host Activity's view
     *
     * Phone only
     */
    QUALAROO_SURVEY_POSITION_TOP,

    /**
     * Attach survey at the bottom of the host Activity's view
     *
     * Phone only
     */
    QUALAROO_SURVEY_POSITION_BOTTOM,

    /**
     * Attach survey at the top left corner of the host Activity's view
     *
     * Tablet only
     */
    QUALAROO_SURVEY_POSITION_TOP_LEFT,

    /**
     * Attach survey at the top right corner of the host Activity's view
     *
     * Tablet only
     */
    QUALAROO_SURVEY_POSITION_TOP_RIGHT,

    /**
     * Attach survey at the bottom right corner of the host Activity's view
     *
     * Tablet only
     */
    QUALAROO_SURVEY_POSITION_BOTTOM_LEFT,

    /**
     * Attach survey at the bottom right corner of the host Activity's view
     *
     * Tablet only     */
    QUALAROO_SURVEY_POSITION_BOTTOM_RIGTH
}