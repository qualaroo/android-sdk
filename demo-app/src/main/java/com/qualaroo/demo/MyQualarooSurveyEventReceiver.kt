package com.qualaroo.demo

import android.util.Log
import com.qualaroo.QualarooSurveyEventReceiver

class MyQualarooSurveyEventReceiver : QualarooSurveyEventReceiver() {

    override fun onSurveyEvent(surveyAlias: String, eventType: Int) {
        when (eventType) {
            EVENT_TYPE_SHOWN -> Log.d("Observer", "$surveyAlias has been shown")
            EVENT_TYPE_DISMISSED -> Log.d("Observer", "$surveyAlias has been dismissed")
            EVENT_TYPE_FINISHED -> Log.d("Observer", "$surveyAlias has been finished")
        }
    }

}
