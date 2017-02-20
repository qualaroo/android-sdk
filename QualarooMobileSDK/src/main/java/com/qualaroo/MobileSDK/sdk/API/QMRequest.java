package com.qualaroo.MobileSDK.sdk.API;

import android.util.Base64;

import com.qualaroo.MobileSDK.sdk.Model.QMSurvey;
import com.qualaroo.MobileSDK.sdk.QMException;
import com.qualaroo.MobileSDK.sdk.QMUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Artem Orynko on 09.12.16.
 * Copyright © 2016 Qualaroo. All rights reserved.
 */

public class QMRequest {

    private static final String BASE_URL = "https://api.qualaroo.com/api/v1/nudges/";
    private static final String END_URL = "/responses.json";

   private String appKey = null;
   private String secretKey = null;

   public QMRequest(String appKey, String secretKey) throws Exception {

       this.appKey = appKey;
       this.secretKey = secretKey;

       HttpURLConnection request = getRequestBySurveyId("0");

       int statusCode = request.getResponseCode();

       if (statusCode == 401) throw new QMUtils(QMException.INVALID_API_SECRET_KEY);
   }

    public void requestSurvey(QMSurvey survey) throws Exception {

        JSONArray responseArray;
        String surveyId = survey.surveyId;
        HttpURLConnection request = getRequestBySurveyId(surveyId);
        int statusCode = request.getResponseCode();

        if (statusCode != 200) throw new QMUtils(QMException.BAD_REQUEST_TO_SERVER);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(request.getInputStream())
        );

        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        responseArray = new JSONArray(response.toString());

        in.close();

        ArrayList<String> identities = new ArrayList<>();

        for (int i = 0; i < responseArray.length(); i++) {

            JSONObject jsonObject;

            try {
                jsonObject = responseArray.getJSONObject(i);

                String identity = jsonObject.getString("identity");

                if (identity.equals("null")) continue;

                identities.add(identity);

            } catch (JSONException ignore) {}
        }

        survey.identity = identities;

    }

  private HttpURLConnection getRequestBySurveyId(String surveyId) throws Exception {

    String urlString = BASE_URL + surveyId + END_URL;
    URL url = new URL(urlString);
    HttpURLConnection request = (HttpURLConnection) url.openConnection();

    request.setRequestMethod("GET");
    request.setRequestProperty("Authorization", getBasicAuthorization());

    return request;
  }

   private String getBasicAuthorization() {
       return "Basic " + Base64.encodeToString((appKey + ":" + secretKey).getBytes(), 0);
   }
}
