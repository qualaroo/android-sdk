package qualaroo.com.AndroidMobileSDK.Api;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import qualaroo.com.AndroidMobileSDK.Model.QMSurvey;
import qualaroo.com.AndroidMobileSDK.QMShowSurvey;

/**
 * Created by Artem Orynko on 09.12.16.
 * Copyright © 2016 Qualaroo. All rights reserved.
 */

public class QMRequest {

    private static final String BASE_URL = "https://api.qualaroo.com/api/v1/nudges/";
    private static final String END_URL = "/responses.json";

    public String mAppKey = null;
    public String mSecretKey = null;

    private static QMRequest instance;

    public static synchronized QMRequest getInstance() {
        if (instance == null) {
            instance = new QMRequest();
        }
        return instance;
    }

    public HashMap getSurveyInfo(String survey) {
        HashMap<String, QMSurvey> dictionary = new HashMap<>();
        JSONObject jsonObject = null;
        JSONObject surveyRequireMaps = null;
        JSONObject surveyAliases = null;
        try {
            jsonObject = new JSONObject(survey);
            surveyRequireMaps = jsonObject.getJSONObject("surveyRequireMaps");
            surveyAliases = jsonObject.getJSONObject("surveyAliases");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Iterator<String> sa = surveyAliases.keys();
        while (sa.hasNext()) {
            String alias = sa.next();
            String id = "";
            try {
                id = surveyAliases.getString(alias);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final QMSurvey qmSurvey = new QMSurvey(alias, id);
            JSONObject json = null;
            try {
                json = new JSONObject(surveyRequireMaps.get(surveyAliases.getString(alias)).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (json.toString().contains("_is_persistent_")) {
                qmSurvey.setHowOftenShowSurvey(QMShowSurvey.QualarooShowSurveyPersistant);
            } else if (json.toString().contains("_is_one_shot_")) {
                qmSurvey.setHowOftenShowSurvey(QMShowSurvey.QualarooShowSurveyOnce);
            } else {
                qmSurvey.setHowOftenShowSurvey(QMShowSurvey.QualarooShowSurveyDefault);
            }
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    requestSurveyInfo(qmSurvey);
                }
            });

            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            dictionary.put(alias, qmSurvey);
        }

        return dictionary;
    }

    private void requestSurveyInfo(QMSurvey survey) {
        String nudgeID = survey.nudgeID;
        String urlString = BASE_URL + nudgeID + END_URL;

        JSONArray responseArray = null;

        try {
            URL url = new URL(urlString);
            HttpURLConnection request = (HttpURLConnection)url.openConnection();

            request.setRequestMethod("GET");
            request.setRequestProperty("Authorization", getBasicAuthorization());
            request.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
            StringBuilder buf = new StringBuilder();
            String line = null;

            while ((line = reader.readLine()) != null) {
                buf.append(line + "\n");
            }

            responseArray = new JSONArray(buf.toString());

            reader.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayList<String> identities = new ArrayList<String>();

        for (int i = 0; i < responseArray.length(); i++) {

            JSONObject response = null;
            try {
                response = responseArray.getJSONObject(i);

                String identity = response.getString("identity");

                if (identity == "null") {
                    continue;
                }

                identities.add(identity);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        survey.identity = identities;
    }

    private String getBasicAuthorization() {
        return "Basic " + Base64.encodeToString((mAppKey + ":" + mSecretKey).getBytes(), 0);
    }
}
