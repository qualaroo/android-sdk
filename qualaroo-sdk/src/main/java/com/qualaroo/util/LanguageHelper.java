package com.qualaroo.util;

import com.qualaroo.internal.model.Language;
import com.qualaroo.internal.model.Survey;

import java.util.List;
import java.util.Locale;

public final class LanguageHelper {
    public static Language getTargetLanguage(Survey survey, Language preferredLanguage) {
        List<Language> languages = survey.spec().surveyVariations();
        if (preferredLanguage != null && languages.contains(preferredLanguage)) {
            return preferredLanguage;
        }
        Language defaultLanguage = new Language(Locale.getDefault().getLanguage());
        if (languages.contains(defaultLanguage)) {
            return defaultLanguage;
        }
        return survey.spec().surveyVariations().get(0);
    }
}
