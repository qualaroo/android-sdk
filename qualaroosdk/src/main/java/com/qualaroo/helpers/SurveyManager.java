/*
 * Copyright © 2017 Qualaroo. All rights reserved.
 */

package com.qualaroo.helpers;
/*
 * Created by Artem Orynko on 4/13/17.
 */

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public final class SurveyManager {

    private static String QUALAROO = "com.qualaroo";

    private static String SURVEY_ALIASES = "surveyAliases";
    private static String SURVEY_REQUIRE_MAPS = "surveyRequireMaps";

    private static String SURVEY_HOW_OFTEN_SHOW = "howOftenShow";

    public static String SURVEY_SHOWN = "shown";
    public static String SURVEY_ANSWERED = "answered";

    private static String HOW_OFTEN_SHOW_ONCE = "_is_one_shot_";
    private static String HOW_OFTEN_SHOW_PERSISTENT = "_is_persistent_";

    public static void saveSurveysInformation(Context context, String information) {
        HashMap<String, HashMap<String, Object>> surveys = getSurveys(context);
        JSONObject aliases = null;
        JSONObject requiredMaps = null;

        try {
            JSONObject jsonObject = new JSONObject(information);
            aliases = jsonObject.getJSONObject(SURVEY_ALIASES);
            requiredMaps = jsonObject.getJSONObject(SURVEY_REQUIRE_MAPS);
        } catch (JSONException ignored) {}

        assert aliases != null;
        Iterator<String> keys = aliases.keys();

        while (keys.hasNext()) {
            String alias = keys.next();
            String id = null;
            JSONObject howOftenShowSurvey = null;

            HashMap<String, Object> survey = surveys.get(alias);

            if (survey == null) {
                survey = new HashMap<>();
            }

            try {
                id = aliases.getString(alias);
            } catch (JSONException ignored) {}

            survey.put(SURVEY_HOW_OFTEN_SHOW, HowOftenShowSurvey.DEFAULT.toString());

            assert requiredMaps != null;
            try {
                howOftenShowSurvey = new JSONObject(requiredMaps.get(id).toString());
            } catch (JSONException ignored) {}

            assert howOftenShowSurvey != null;
            if (howOftenShowSurvey.toString().contains(HOW_OFTEN_SHOW_ONCE)) {
                survey.put(SURVEY_HOW_OFTEN_SHOW, HowOftenShowSurvey.ONCE.toString());
            } else if (howOftenShowSurvey.toString().contains(HOW_OFTEN_SHOW_PERSISTENT)) {
                survey.put(SURVEY_HOW_OFTEN_SHOW, HowOftenShowSurvey.PERSISTENT.toString());
            }

            assert surveys != null;
            surveys.put(alias, survey);
        }

        String surveysString = new JSONObject(surveys).toString();

        setSurveys(context, surveysString);
    }

    public static void addInformation(Context context, String identifier, String type, String alias) {
        HashMap<String, HashMap<String, Object>> surveys = getSurveys(context);
        HashMap<String, Object> survey = surveys.get(alias);
        HashSet<String> ids = (HashSet<String>) survey.get(type);

        if (ids == null) {
            ids = new HashSet<>();
        }
        ids.add(identifier);
        survey.put(type, ids);
        surveys.put(alias, survey);

        String surveysString = new JSONObject(surveys).toString();

        setSurveys(context, surveysString);
    }

    /** Check the given alias exists. */
    public static String isAliasExists(Context context, String alias) {
        String result = null;
        HashMap<String, HashMap<String, Object>> surveys = getSurveys(context);
        int range = 0;

        Iterator iterator = surveys.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Object> survey = (Map.Entry<String, Object>) iterator.next();
            String surveyAlias = survey.getKey();

            if (surveyAlias.toLowerCase().contains(alias.toLowerCase())) {
                int newRange = surveyAlias.length();

                if (newRange > range) {
                    result = surveyAlias;
                }
            }
        }
        return result;
    }

    /** Is showing the survey by given alias. */
    public static boolean isShowingSurvey(Context context, String alias, String identifier, Logger logger) {
        HashMap<String, HashMap<String, Object>> surveys = getSurveys(context);
        HashMap<String, Object> survey = surveys.get(alias);
        String howOftenShowSurvey = survey.get(SURVEY_HOW_OFTEN_SHOW).toString();

        if (!howOftenShowSurvey.equals(HowOftenShowSurvey.PERSISTENT.toString())) {
            boolean isOneShot = howOftenShowSurvey.equals(HowOftenShowSurvey.ONCE.toString());
            String action = isOneShot ? SURVEY_ANSWERED : SURVEY_SHOWN;

            HashSet<String> identifiers = getIdentifiers(action, survey);

            if (identifiers.size() == 0) return true;

            if (identifiers.contains(identifier)) {
                if (action.equals(SURVEY_SHOWN)) {
                    logger.info("Survey have already is shown for this customer.");
                } else {
                    logger.info("Customer already answered for this survey.");
                }
                return false;
            }
        }
        return true;
    }

    private static HashSet<String> getIdentifiers(String type, HashMap<String, Object> survey) {
        HashSet<String> identifiers = new HashSet<>();
        JSONArray idsArray = null;
        Object ids = survey.get(type);

        if (ids == null) {
            return identifiers;
        }
        try {
            idsArray = new JSONArray(ids.toString());
        } catch (JSONException ignored) {
            return identifiers;
        }

        assert idsArray != null;
        if (idsArray.length() == 0) {
            return identifiers;
        }
        for (int i = 0; i < idsArray.length(); i++) {
            try {
                identifiers.add(idsArray.getString(i));
            } catch (JSONException ignored) {}
        }
        return identifiers;
    }

    private static void setSurveys(Context context, String surveys) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(QUALAROO, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(QUALAROO, surveys).apply();
    }

    private static HashMap<String, HashMap<String, Object>> getSurveys(Context context) {
        HashMap<String, HashMap<String, Object>> surveys = new HashMap<>();
        String surveysString = null;
        SharedPreferences sharedPreferences = context.getSharedPreferences(QUALAROO, Context.MODE_PRIVATE);

        surveysString = sharedPreferences.getString(QUALAROO, null);

        if (surveysString == null) {
            return new HashMap<>();
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(surveysString);
        } catch (JSONException ignored) {
            return new HashMap<>();
        }
        Iterator<String> keys = jsonObject.keys();

        while (keys.hasNext()) {
            HashMap<String, Object> survey = new HashMap<>();
            String alias = keys.next();
            JSONObject surveyObject = null;
            String howOftenShowSurveyObject = null;
            JSONArray shownArray = null;
            JSONArray answeredArray = null;
            try {
                String surveyString = String.valueOf(jsonObject.get(alias));
                surveyObject = new JSONObject(surveyString);
                howOftenShowSurveyObject = surveyObject.getString(SURVEY_HOW_OFTEN_SHOW);
            } catch (JSONException ignored) {}

            assert surveyObject != null;
            try {
                shownArray = surveyObject.getJSONArray(SURVEY_SHOWN);
            } catch (JSONException ignored) {}
            try {
                answeredArray = surveyObject.getJSONArray(SURVEY_ANSWERED);
            } catch (JSONException ignored) {}

            if (howOftenShowSurveyObject != null && !howOftenShowSurveyObject.equals("null")) {
                survey.put(SURVEY_HOW_OFTEN_SHOW, howOftenShowSurveyObject);
            }
            if (shownArray != null) {
                HashSet<String> ids = new HashSet<>();
                for (int i = 0; i < shownArray.length(); i++) {
                    String id = null;
                    try {
                        id = shownArray.getString(i);
                    } catch (JSONException ignored) { break; }

                    ids.add(id);
                }
                survey.put(SURVEY_SHOWN, ids);
            }
            if (answeredArray != null) {
                HashSet<String> ids = new HashSet<>();
                for (int i = 0; i < answeredArray.length(); i ++) {
                    String id = null;
                    try {
                        id = answeredArray.getString(i);
                    } catch (JSONException ignored) { break; }

                    ids.add(id);
                }
                survey.put(SURVEY_ANSWERED, ids);
            }
            surveys.put(alias, survey);
        }
        return surveys;
    }

    private enum HowOftenShowSurvey {
        ONCE,
        DEFAULT,
        PERSISTENT;
    }
}
