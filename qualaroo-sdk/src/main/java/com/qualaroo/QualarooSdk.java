package com.qualaroo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.util.List;

public interface QualarooSdk {

    /**
     * Returns a list of all available surveys aliases.
     * @return list of all available surveys aliases
     */
    @NonNull
    @WorkerThread
    List<String> getSurveysAliases();

    /**
     * Allows to check whether specific survey will be displayed after showSurvey call
     * @param alias alias of the survey
     */
    @WorkerThread
    boolean willSurveyBeShown(@NonNull String alias);

    /**
     * Shows a survey with specified alias if all targeting conditions are met.
     * @param alias alias of a survey that should be displayed
     */
    void showSurvey(@NonNull String alias);

    /**
     * Shows a survey with a specified alias.
     * @param alias alias of a survey that should be displayed
     * @param options various options to modify behaviour of a survey
     */
    void showSurvey(@NonNull String alias, @NonNull SurveyOptions options);

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
     * Fallbacks to user's locale or the first available language if default is not found in a survey.
     * @param iso2Language ISO 639-1 compatible language code (two lowercase letters)
     *                     Passing null value will remove preferred language.
     */
    void setPreferredLanguage(@Nullable String iso2Language);

    /**
     * Starts a configuration of an AB test.
     * @return an instance of {@link QualarooSdk.AbTestBuilder}
     */
    AbTestBuilder abTest();

    interface Builder {
        /**
         * Sets an API key.
         * You are required to set a key to be able to work with the SDK.
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

    interface AbTestBuilder {

        /**
         * @param aliases of surveys that you want to AB test.
         * @return {@link QualarooSdk.AbTestBuilder} for further configuration
         */
        AbTestBuilder fromSurveys(List<String> aliases);

        /**
         * Shows one of a provided surveys by {@link AbTestBuilder#fromSurveys(List)} call.
         * A survey will be chosen based on a random test group that a user is assigned to.
         * You can configure a chance of a specific survey to be shown to a user by adjusting percentage of all visitors
         * in Qualaroo's Dashboard targeting tab.
         *
         * Keep in mind that sum of percentages of all surveys that you want to AB test should be no higher than 100.
         *
         * An example (invalid):
         * "my_survey_1" - 50%
         * "my_survey_2" - 50%
         * "my_survey_3" - 50%
         *
         * With this configuration, only first and second surveys will be taken into an account.
         * First test group will be in [0-50) range, while the other will be [50,100).
         *
         * Correct version of an above example:
         * "my_survey_1" - 30%
         * "my_survey_2" - 30%
         * "my_survey_3" - 40%
         */
        void show();

    }
}
