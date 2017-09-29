package qualaroo.com.QualarooMobileDemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.qualaroo.Qualaroo;

public class SurveyDemoActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        Qualaroo.getInstance().setUserProperty("something", "gold");
        findViewById(R.id.show_surveys_button).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Qualaroo.getInstance().showSurvey("mobile_test_survey");
            }
        });
    }
}
