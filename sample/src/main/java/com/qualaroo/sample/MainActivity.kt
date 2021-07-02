package com.qualaroo.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.qualaroo.Qualaroo
import com.qualaroo.SurveyOptions

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.main_hello_button).setOnClickListener {
            val options = SurveyOptions.Builder()
                    .ignoreSurveyTargeting(true)
                    .build()
            Qualaroo.getInstance().showSurvey("<survey_alias>", options)
        }
    }
}
