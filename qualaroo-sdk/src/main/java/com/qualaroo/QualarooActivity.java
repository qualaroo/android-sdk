package com.qualaroo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.qualaroo.internal.model.Survey;
import com.qualaroo.ui.SurveyComponent;
import com.qualaroo.ui.SurveyFragment;

public class QualarooActivity extends AppCompatActivity {

    private static final String KEY_SURVEY = "com.qualaroo.survey";

    public static void showSurvey(Context context, Survey survey) {
        Intent starter = new Intent(context, QualarooActivity.class);
        starter.putExtra(KEY_SURVEY, survey);
        context.startActivity(starter);
    }

    private SurveyComponent surveyComponent;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        surveyComponent = (SurveyComponent) getLastCustomNonConfigurationInstance();
        if (surveyComponent == null) {
            surveyComponent = SurveyComponent.from((Survey) getIntent().getExtras().getSerializable(KEY_SURVEY));
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, new SurveyFragment(), "survey")
                    .commit();
        }
    }

    @Override public Object getSystemService(@NonNull String name) {
        if (SurveyComponent.class.getName().equals(name)) {
            return surveyComponent;
        }
        return super.getSystemService(name);
    }

    @Override public Object onRetainCustomNonConfigurationInstance() {
        return surveyComponent;
    }

    @Override public void onBackPressed() {
        SurveyFragment surveyFragment = (SurveyFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
        boolean handledByFragment = surveyFragment.onBackPressed();
        if (!handledByFragment) {
            super.onBackPressed();
        }
    }
}
