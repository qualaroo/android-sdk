package com.qualaroo;

import android.support.annotation.NonNull;

public interface QualarooBase {
    /**
     * Shows a survey with specified alias if all targeting conditions are met.
     * @param alias - alias of a survey that should be shown
     */
    void showSurvey(@NonNull String alias);

    /**
     * Sets unique user id for tracking purposes.
     * @param userId - unique user's id
     */
    void setUserId(@NonNull String userId);

    /**
     * Sets a single custom user property to a specific value.
     * Stored values can be later used for targeting and tracking purposes.
     * @param key - name of the property
     * @param value - value of the property
     */
    void setUserProperty(@NonNull String key, String value);

    /**
     * Sets default language for surveys.
     * Fallbacks to English or the first available language if default is not found in a survey.
     * @param iso2Language - ISO 639-1 compatible language code (two lowercase letters)
     */
    void setPreferredLanguage(@NonNull String iso2Language);
}
