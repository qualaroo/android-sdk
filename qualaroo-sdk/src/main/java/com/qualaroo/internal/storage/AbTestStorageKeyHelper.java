package com.qualaroo.internal.storage;

import com.qualaroo.internal.model.Survey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class AbTestStorageKeyHelper {

    private AbTestStorageKeyHelper() {}

    public static String build(List<Survey> surveys) {
        List<Survey> surveysCopy = new ArrayList<>(surveys);
        Collections.sort(surveysCopy, new Comparator<Survey>() {
            @Override public int compare(Survey o1, Survey o2) {
                return o1.id() - o2.id();
            }
        });
        StringBuilder keyBuilder = new StringBuilder();
        for (Survey survey : surveysCopy) {
            keyBuilder.append(survey.id());
            keyBuilder.append('/');
        }
        return keyBuilder.toString();
    }

}
