package com.qualaroo.sample

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.qualaroo.Qualaroo
import com.qualaroo.SurveyOptions

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.main_hello_button).setOnClickListener {
            Qualaroo.getInstance().showSurvey("emoji_thumb_test")
        }
    }
}
