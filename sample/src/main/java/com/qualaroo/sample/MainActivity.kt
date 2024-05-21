package com.qualaroo.sample

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.qualaroo.Qualaroo

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.main_hello_button).setOnClickListener {
            Qualaroo.getInstance().showSurvey("alias key")
        }
    }
}
