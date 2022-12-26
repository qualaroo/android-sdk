package com.qualaroo.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import com.qualaroo.Qualaroo
import com.qualaroo.SurveyOptions

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.main_hello_button).setOnClickListener {
            val res =   Qualaroo.getInstance().isSurveyShown("229172")
            if(!res){
                Qualaroo.getInstance().showSurvey("show_once_testing")
            } else {
                Toast.makeText(applicationContext,"Already Seen", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
