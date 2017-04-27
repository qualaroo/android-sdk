package qualaroo.com.QualarooMobileDemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.qualaroo.Qualaroo;

public class Survey extends AppCompatActivity {

    EditText surveyAlias;
    Button showSurvey;
    Qualaroo qualaroo = Qualaroo.with(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        Qualaroo.with(this).setPosition(Qualaroo.Position.BOTTOM);

        surveyAlias = (EditText) findViewById(R.id.surveyAlias);
        showSurvey = (Button) findViewById(R.id.showSurvey);

        showSurvey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qualaroo.showSurvey(surveyAlias.getText().toString(), true);
            }
        });
    }
}
