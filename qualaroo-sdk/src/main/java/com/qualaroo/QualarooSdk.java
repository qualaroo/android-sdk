package com.qualaroo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface QualarooSdk {
    /**
     * Shows a survey with specified alias if all targeting conditions are met.
     * @param alias alias of a survey that should be displayed
     */
    void showSurvey(@NonNull String alias);

    /**
     * Sets unique user id for tracking purposes.
     * @param userId unique user's id
     */
    void setUserId(@NonNull String userId);

    /**
     * Sets a single custom user property to a specific value.
     * Stored values can be later used for targeting and tracking purposes.
     * Passing null values will implicitly have the same effect as calling {@link #removeUserProperty(String)} method.
     * @param key name of the property
     * @param value value of the property
     */
    void setUserProperty(@NonNull String key, @Nullable String value);

    /**
     * Removes stored user property.
     * @param key name of the property to be removed
     */
    void removeUserProperty(@NonNull String key);

    /**
     * Sets default language for surveys.
     * Fallbacks to English or the first available language if default is not found in a survey.
     * @param iso2Language ISO 639-1 compatible language code (two lowercase letters)
     */
    void setPreferredLanguage(@NonNull String iso2Language);

    interface Builder {
        /**
         * Sets an API key.
         * You are required to set a key to be able to work with the SDK.
         * @throws com.qualaroo.internal.InvalidCredentialsException on malformed or invalid key
         * @return {@link QualarooSdk.Builder} that you can use to configure the SDK.
         */
        Builder setApiKey(String apiKey);

        /**
         * Turns on the debug mode.
         * Debug mode will cause Qualaroo SDK to log survey related events in a console.
         * @param isDebugMode false by default
         * @return {@link QualarooSdk.Builder} that you can use to configure the SDK.
         */
        Builder setDebugMode(boolean isDebugMode);

        /**
         * Initializes the SDK.
         * This method has an effect only once. Configured instance of QualarooSdk is then stored per application's process.
         */
        void init();
    }
}
