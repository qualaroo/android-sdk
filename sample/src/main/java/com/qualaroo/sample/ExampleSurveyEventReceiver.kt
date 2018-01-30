package com.qualaroo.sample

import android.util.Log
import com.qualaroo.QualarooSurveyEventReceiver

class ExampleSurveyEventReceiver : QualarooSurveyEventReceiver() {

    override fun onSurveyEvent(surveyAlias: String, eventType: Int) {
        when (eventType) {
            EVENT_TYPE_SHOWN -> log("$surveyAlias has been shown")
            EVENT_TYPE_DISMISSED -> log("$surveyAlias has been dismissed")
            EVENT_TYPE_FINISHED -> log("$surveyAlias has been finished")
        }
    }

    private fun log(msg: String) {
        Log.d("SurveyEventReceiver", msg)
    }
}
