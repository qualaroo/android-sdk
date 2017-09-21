package qualaroo.com.QualarooMobileDemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.qualaroo.Inject;
import com.qualaroo.Qualaroo;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.util.DebouncingOnClickListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SurveyActivity extends AppCompatActivity {

    private static final String KEY_RECENT_SURVEY = "recentSurvey";

    private SurveysAdapter surveysAdapter;
    private SharedPreferences sharedPreferences;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        sharedPreferences = getSharedPreferences("qualaroo_demo", Context.MODE_PRIVATE);
        final Button showSurveysButton = findViewById(R.id.show_surveys_button);
        final Spinner availableSurveysSpinner = findViewById(R.id.surveys_spinner);
        surveysAdapter = new SurveysAdapter(this);
        availableSurveysSpinner.setAdapter(surveysAdapter);
        final Qualaroo qualaroo = Inject.qualaroo(this);
        showSurveysButton.setEnabled(false);
        showSurveysButton.setOnClickListener(new DebouncingOnClickListener() {
            @Override public void doClick(View v) {
                Survey survey = (Survey) availableSurveysSpinner.getSelectedItem();
                sharedPreferences.edit().putString(KEY_RECENT_SURVEY, survey.canonicalName()).apply();
                qualaroo.showSurvey(SurveyActivity.this, survey);
            }
        });
        qualaroo.setSurveysListener(new Qualaroo.SurveysListener() {
            @Override public void onSurveysReady(List<Survey> surveys) {
                showSurveysButton.setEnabled(true);
                String recentSurveyName = sharedPreferences.getString(KEY_RECENT_SURVEY, null);
                if (recentSurveyName != null) {
                    LinkedList<Survey> surveysToDisplay = new LinkedList<>();
                    for (Survey survey : surveys) {
                        if (recentSurveyName.equals(survey.canonicalName())) {
                            surveysToDisplay.addFirst(survey);
                        } else {
                            surveysToDisplay.addLast(survey);
                        }
                        surveysAdapter.setSurveys(surveysToDisplay);
                    }
                } else {
                    surveysAdapter.setSurveys(surveys);
                }
            }
        });
        qualaroo.init();

        findViewById(R.id.open_sandbox).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                getSupportFragmentManager().beginTransaction()
                        .add(android.R.id.content, new SandboxFragment())
                        .commitNow();
            }
        });

    }

    private static class SurveysAdapter extends ArrayAdapter<Survey> {

        private final List<Survey> surveys = new ArrayList<>();

        SurveysAdapter(@NonNull Context context) {
            super(context, android.R.layout.simple_list_item_1);
        }

        @Nullable @Override public Survey getItem(int position) {
            return surveys.get(position);
        }

        @Override public int getCount() {
            return surveys.size();
        }

        void setSurveys(List<Survey> surveys) {
            this.surveys.clear();
            this.surveys.addAll(surveys);
            notifyDataSetChanged();
        }

    }
}
